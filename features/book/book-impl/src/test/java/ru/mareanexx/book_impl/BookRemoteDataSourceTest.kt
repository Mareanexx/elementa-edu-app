package ru.mareanexx.book_impl

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.*
import org.junit.Assert.*
import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_impl.data.BookRepositoryFactory
import java.util.concurrent.TimeUnit

class BookRemoteDataSourceTest {
    private lateinit var server: MockWebServer
    @Before fun setup() { server = MockWebServer(); server.start() }
    @After fun cleanup() { server.shutdown() }
    private fun repository(timeout: Long = 2000) = BookRepositoryFactory.create(OkHttpClient.Builder().callTimeout(timeout, TimeUnit.MILLISECONDS).retryOnConnectionFailure(false).build(), server.url("/").toString())
    private fun body(value: String) { server.enqueue(MockResponse().setBody(value)) }
    @Test fun `книга загружается по идентификатору а прогресс ограничен числом страниц`() = runBlocking {
        body(bookJson)
        val book = (repository().getBook("book_1") as BookResult.Success).value
        assertEquals("Органическая химия", book.title)
        assertEquals(160, book.pagesRead)
        assertEquals(64, book.rating.count)
        assertEquals("/api/v1/books/book_1", server.takeRequest().path)
    }
    @Test fun `повреждённая статистика не попадает в интерфейс`() = runBlocking {
        body(bookJson.replace("[1,3,8,22,30]", "[1,2]"))
        assertEquals(BookResult.Failure(BookError.InvalidResponse), repository().getBook("book_1"))
    }
    @Test fun `избранное использует идемпотентные PUT и DELETE`() = runBlocking {
        val repo = repository()
        body(shelvesJson); repo.setFavorite("book_1", true)
        val put = server.takeRequest()
        assertEquals("PUT", put.method)
        assertEquals("/api/v1/users/me/books/book_1/favorite", put.path)
        body(shelvesJson); repo.setFavorite("book_1", false)
        assertEquals("DELETE", server.takeRequest().method)
    }
    @Test fun `отмена реакции отправляет явный null`() = runBlocking {
        body(reviewJson)
        repository().react("book_1", "r1", null)
        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/v1/books/book_1/reviews/r1/reaction", request.path)
        assertEquals("{\"value\":null}", request.body.readUtf8())
    }
    @Test fun `фильтр сортировка и курсор передаются в запрос`() = runBlocking {
        body("""{"items":[],"next_cursor":null,"total_count":0}""")
        repository().getReviews("book_1", ReviewsQuery(ReviewSort.Lowest, 2), "last-review")
        val url = server.takeRequest().requestUrl!!
        assertEquals("rating_asc", url.queryParameter("sort"))
        assertEquals("2", url.queryParameter("rating"))
        assertEquals("last-review", url.queryParameter("cursor"))
        assertEquals("10", url.queryParameter("limit"))
    }
    @Test fun `ключ повторной отправки ответа сохраняется в заголовке`() = runBlocking {
        val key = "c5815330-c29e-45e9-a7b7-8a7f480f412d"
        val repo = repository()
        repeat(2) {
            body(replyJson)
            repo.reply("book_1", "r1", "  Спасибо  ", key)
            val request = server.takeRequest()
            assertEquals(key, request.getHeader("Idempotency-Key"))
            assertEquals("{\"text\":\"Спасибо\"}", request.body.readUtf8())
        }
    }
    @Test fun `истёкшая сессия и ограничение частоты имеют отдельные ошибки`() = runBlocking {
        val repo = repository()
        server.enqueue(MockResponse().setResponseCode(401))
        assertEquals(BookResult.Failure(BookError.Unauthorized), repo.getBook("book_1"))
        server.enqueue(MockResponse().setResponseCode(429))
        assertEquals(BookResult.Failure(BookError.RateLimited), repo.setWantToRead("book_1", true))
    }
    @Test fun `таймаут всего вызова отличается от отсутствия сети`() = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        assertEquals(BookResult.Failure(BookError.Timeout), repository(150).getBook("book_1"))
    }
    @Test fun `невалидные поля не отправляют HTTP запрос`() = runBlocking {
        val repo = repository()
        assertEquals(BookResult.Failure(BookError.RatingRequired), repo.saveReview("book_1", ReviewInput(0, "")))
        assertEquals(BookResult.Failure(BookError.ReplyRequired), repo.reply("book_1", "r1", "  ", "key"))
        assertEquals(0, server.requestCount)
    }
    @Test fun `событие обновления библиотеки возникает только после успеха`() = runBlocking {
        val repo = repository()
        var count = 0
        val observer = launch(Dispatchers.Unconfined) { repo.libraryChanges.collect { count++ } }
        body(shelvesJson); repo.setWantToRead("book_1", true)
        server.enqueue(MockResponse().setResponseCode(500)); repo.setFavorite("book_1", true)
        assertEquals(1, count)
        observer.cancelAndJoin()
    }

    private val shelvesJson = """{"favorite":true,"want_to_read":false,"collection_count":1}"""
    private val bookJson = """{"id":"book_1","title":"Органическая химия","authors":"Авторы","description":"Описание","images":[],"tags":[],"pages":160,"pages_read":999,"file_bytes":5600000,"notes_count":12,"rating":{"average":4.2,"count":64,"distribution":[1,3,8,22,30]},"shelves":{"favorite":false,"want_to_read":false,"collection_count":0},"metadata":{"publisher":null,"year":2022,"language":"Русский","isbn":null,"format":"EPUB"},"my_review":null,"preview_reviews":[]}"""
    private val reviewJson = """{"id":"r1","author_id":"a","author_name":"Анна","avatar_url":null,"rating":5,"text":"Пример","created_at":"2026-01-01T12:00:00Z","updated_at":null,"likes":3,"dislikes":1,"reaction":null,"reply_count":0,"is_mine":false}"""
    private val replyJson = """{"id":"reply1","author_name":"Мария","avatar_url":null,"text":"Спасибо","created_at":"2026-01-01T12:00:00Z","is_mine":true}"""
}
