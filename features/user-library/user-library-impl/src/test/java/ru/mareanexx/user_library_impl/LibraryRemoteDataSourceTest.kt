package ru.mareanexx.user_library_impl

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_impl.data.remote.api.LibraryApi
import ru.mareanexx.user_library_impl.data.remote.dto.*
import ru.mareanexx.user_library_impl.data.remote.source.LibraryRemoteDataSource
import java.util.concurrent.TimeUnit

class LibraryRemoteDataSourceTest {
    private lateinit var server: MockWebServer
    private val gson = Gson()
    @Before fun setup() { server = MockWebServer(); server.start() }
    @After fun cleanup() { server.shutdown() }
    private fun remote(timeout: Long = 1000): LibraryRemoteDataSource {
        val client = OkHttpClient.Builder().readTimeout(timeout, TimeUnit.MILLISECONDS).retryOnConnectionFailure(false).build()
        val api = Retrofit.Builder().baseUrl(server.url("/")).client(client).addConverterFactory(GsonConverterFactory.create()).build().create(LibraryApi::class.java)
        return LibraryRemoteDataSource(api)
    }
    private fun book() = LibraryBookDto("book_1", "Органическая химия", "Мария Белова", 160, 48, "in_progress", null, "chemistry", LibraryRatingDto(4.5, 10), emptyList(), false, false, 0)
    private fun page() = LibraryPageDto(listOf(book()), null, 1, LibraryCountsDto(1, 1, 0, 0), null)
    private fun enqueue(value: Any) { server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody(gson.toJson(value))) }
    @Test fun `поиск передаёт нормализованный запрос фильтр сортировку курсор и размер страницы`() = runBlocking {
        enqueue(page())
        assertTrue(remote().books(LibraryQuery(LibraryCategory.InProgress, "  Мария   химия  ", LibrarySort.Author), "opaque") is LibraryResult.Success)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/v1/users/me/library", request.requestUrl!!.encodedPath)
        assertEquals("Мария химия", request.requestUrl!!.queryParameter("query"))
        assertEquals("in_progress", request.requestUrl!!.queryParameter("status"))
        assertEquals("author_asc", request.requestUrl!!.queryParameter("sort"))
        assertEquals("opaque", request.requestUrl!!.queryParameter("cursor"))
        assertEquals("20", request.requestUrl!!.queryParameter("limit"))
    }
    @Test fun `одна буква не отправляется на сервер`() = runBlocking {
        assertEquals(LibraryResult.Failure(LibraryError.InvalidResponse), remote().books(LibraryQuery(search = "Я"), null))
        assertEquals(0, server.requestCount)
    }
    @Test fun `сортировка сохраняется PATCH запросом`() = runBlocking {
        enqueue(LibraryPreferencesDto("title_asc"))
        assertEquals(LibraryResult.Success(LibraryPreferences(LibrarySort.Title)), remote().savePreferences(LibraryPreferences(LibrarySort.Title)))
        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/api/v1/users/me/library/preferences", request.path)
        assertEquals("title_asc", com.google.gson.JsonParser.parseString(request.body.readUtf8()).asJsonObject.get("sort_order").asString)
    }
    @Test fun `отметка прочитанной меняет только статус выбранной книги`() = runBlocking {
        enqueue(book().copy(pagesRead = 160, status = "completed"))
        val result = remote().status("book_1", ReadingStatus.Completed) as LibraryResult.Success
        assertEquals(160, result.value.pagesRead)
        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/v1/users/me/library/books/book_1/reading-status", request.path)
        assertEquals("completed", com.google.gson.JsonParser.parseString(request.body.readUtf8()).asJsonObject.get("status").asString)
    }
    @Test fun `ошибки сессии лимита и курсора различаются`() = runBlocking {
        for ((status, error) in listOf(401 to LibraryError.Unauthorized, 429 to LibraryError.RateLimited, 409 to LibraryError.Conflict)) {
            server.enqueue(MockResponse().setResponseCode(status))
            assertEquals(LibraryResult.Failure(error), remote().books(LibraryQuery(), null))
        }
    }
    @Test fun `некорректные счётчики и статусы не попадают в UI`() = runBlocking {
        enqueue(page().copy(counts = LibraryCountsDto(1, 2, 0, 0)))
        assertEquals(LibraryResult.Failure(LibraryError.InvalidResponse), remote().books(LibraryQuery(), null))
        enqueue(page().copy(items = listOf(book().copy(status = "alien"))))
        assertEquals(LibraryResult.Failure(LibraryError.InvalidResponse), remote().books(LibraryQuery(), null))
    }
    @Test fun `тайм-аут отличается от некорректного ответа`() = runBlocking {
        server.enqueue(MockResponse().setBody(gson.toJson(page())).setBodyDelay(300, TimeUnit.MILLISECONDS))
        assertEquals(LibraryResult.Failure(LibraryError.Timeout), remote(50).books(LibraryQuery(), null))
        server.enqueue(MockResponse().setBody("not json"))
        assertEquals(LibraryResult.Failure(LibraryError.InvalidResponse), remote().books(LibraryQuery(), null))
    }
}
