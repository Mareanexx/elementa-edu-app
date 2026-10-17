package ru.mareanexx.collections_impl

import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.mareanexx.collections_api.domain.model.*
import ru.mareanexx.collections_impl.data.remote.api.CollectionsApi
import ru.mareanexx.collections_impl.data.remote.source.CollectionsRemoteDataSource

class CollectionsRemoteDataSourceTest {
    private lateinit var server: MockWebServer
    private lateinit var source: CollectionsRemoteDataSource
    @Before fun setup() {
        server = MockWebServer()
        server.start()
        source = CollectionsRemoteDataSource(Retrofit.Builder().baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().retryOnConnectionFailure(false).build())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().create())).build().create(CollectionsApi::class.java))
    }
    @After fun cleanup() { server.shutdown() }
    @Test fun `список проверяет обязательное избранное и уникальные идентификаторы`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"items":[{"id":"fav","name":"Избранное","kind":"favorite","cover_key":"future","book_count":0}]}"""))
        val result = source.getCollections() as CollectionsResult.Success
        assertEquals(CollectionCover.Chemistry, result.value.first().cover)
        assertEquals("/api/v1/users/me/collections", server.takeRequest().path)
        server.enqueue(MockResponse().setBody("""{"items":[]}"""))
        assertEquals(CollectionsResult.Failure(CollectionsError.InvalidResponse), source.getCollections())
    }
    @Test fun `ошибки полей и авторизации имеют отдельные коды`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(409).setBody("""{"error":{"code":"NAME_TAKEN"}}"""))
        assertEquals(CollectionsResult.Failure(CollectionsError.NameTaken), source.create(CollectionForm("Книга")))
        server.enqueue(MockResponse().setResponseCode(401).setBody("не json"))
        assertEquals(CollectionsResult.Failure(CollectionsError.Unauthorized), source.getCollections())
        server.enqueue(MockResponse().setResponseCode(429))
        assertEquals(CollectionsResult.Failure(CollectionsError.RateLimited), source.getLibrary())
    }
    @Test fun `пустой успешный ответ удаления не требует json`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(204))
        assertEquals(CollectionsResult.Success(Unit), source.delete("abc"))
        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/v1/users/me/collections/abc", request.path)
    }
    @Test fun `добавление отправляет уникальные идентификаторы`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(204))
        assertEquals(CollectionsResult.Success(Unit), source.addBooks("abc", setOf("b", "a", "b")))
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("""{"book_ids":["a","b"]}""", request.body.readUtf8())
    }
    @Test fun `настройки читаются и изменяются запросами`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"sort_order":"books_desc","show_empty":false}"""))
        assertEquals(CollectionsResult.Success(CollectionsPreferences(CollectionSort.BooksDescending, false)), source.getPreferences())
        assertEquals("GET", server.takeRequest().method)
        server.enqueue(MockResponse().setBody("""{"sort_order":"name_desc","show_empty":true}"""))
        assertEquals(CollectionsResult.Success(CollectionsPreferences(CollectionSort.NameDescending)), source.setPreferences(CollectionsPreferences(CollectionSort.NameDescending)))
        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/api/v1/users/me/collection-preferences", request.path)
        assertEquals("""{"sort_order":"name_desc","show_empty":true}""", request.body.readUtf8())
    }
    @Test fun `книга получает рейтинг и типизированные теги из ответа`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"items":[{"id":"book","title":"Химия","authors":"Авторы","description":"Описание","pages":240,"cover_key":"space","rating":{"average":4.7,"count":128},"tags":[{"id":"advanced","label":"Продвинутый","kind":"difficulty"},{"id":"space","label":"Космос","kind":"topic"}]}]}"""))
        val book = (source.getBooks("fav") as CollectionsResult.Success).value.single()
        assertEquals(BookRating(4.7, 128), book.rating)
        assertEquals(listOf(BookTagKind.Difficulty, BookTagKind.Topic), book.tags.map { it.kind })
        assertEquals(listOf("Продвинутый", "Космос"), book.tags.map { it.label })
    }
    @Test fun `отсутствующая или некорректная необязательная метаинформация не скрывает книгу`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"items":[{"id":"book","title":"Химия","authors":"Авторы","description":"Описание","pages":240,"rating":{"average":9,"count":0},"tags":[null,{"id":"space","label":"Космос","kind":"future"},{"id":"space","label":"Повтор","kind":"topic"},{"id":"bad","label":" ","kind":"topic"}]}]}"""))
        val book = (source.getLibrary() as CollectionsResult.Success).value.single()
        assertNull(book.rating)
        assertEquals(listOf(BookTag("space", "Космос", BookTagKind.Topic)), book.tags)
        server.enqueue(MockResponse().setBody("""{"items":[{"id":"book","title":"Химия","authors":"Авторы","description":"Описание","pages":240}]}"""))
        val legacy = (source.getLibrary() as CollectionsResult.Success).value.single()
        assertNull(legacy.rating)
        assertTrue(legacy.tags.isEmpty())
    }
    @Test fun `неполный ответ и поврежденный json становятся ошибкой данных`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"items":[{"id":"x"}]}"""))
        assertEquals(CollectionsResult.Failure(CollectionsError.InvalidResponse), source.getLibrary())
        server.enqueue(MockResponse().setBody("{broken"))
        assertEquals(CollectionsResult.Failure(CollectionsError.InvalidResponse), source.getCollections())
    }
    @Test fun `обложка отправляется multipart запросом а шаблон явно очищает фото`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(201).setBody("""{"id":"image","url":"https://example.test/cover.jpg"}"""))
        assertEquals(CollectionsResult.Success(CollectionImage("image", "https://example.test/cover.jpg")), source.uploadCover(CollectionCoverUpload(byteArrayOf(1, 2, 3))))
        val upload = server.takeRequest()
        assertEquals("/api/v1/users/me/collection-covers", upload.path)
        assertTrue(upload.getHeader("Content-Type")!!.startsWith("multipart/form-data"))
        assertTrue(upload.body.readUtf8().contains("name=\"image\""))
        server.enqueue(MockResponse().setBody("""{"id":"c","name":"Фото","cover_key":"space","book_count":0,"kind":"custom"}"""))
        source.update("c", CollectionForm("Фото", CollectionCover.Space))
        assertTrue(server.takeRequest().body.readUtf8().contains("\"cover_image_id\":null"))
    }
    @Test fun `прогресс ограничен количеством страниц а поврежденное фото не отправляется`() = runBlocking {
        assertEquals(CollectionsResult.Failure(CollectionsError.InvalidImage), source.uploadCover(CollectionCoverUpload(byteArrayOf())))
        assertEquals(0, server.requestCount)
        server.enqueue(MockResponse().setBody("""{"items":[{"id":"book","title":"Химия","authors":"Автор","description":"Текст","pages":160,"pages_read":999}]}"""))
        assertEquals(160, (source.getBooks("c") as CollectionsResult.Success).value.single().pagesRead)
    }

}
