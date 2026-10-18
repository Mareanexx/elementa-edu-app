package ru.mareanexx.catalog_impl

import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_impl.data.remote.api.CatalogApi
import ru.mareanexx.catalog_impl.data.remote.dto.*
import ru.mareanexx.catalog_impl.data.remote.source.CatalogRemoteDataSource
import java.util.concurrent.TimeUnit

class CatalogRemoteDataSourceTest {
    private lateinit var server: MockWebServer
    private val gson = Gson()
    @Before fun setup() { server = MockWebServer(); server.start() }
    @After fun cleanup() { server.shutdown() }
    private fun remote(timeout: Long = 1000): CatalogRemoteDataSource {
        val api = Retrofit.Builder().baseUrl(server.url("/")).client(OkHttpClient.Builder().readTimeout(timeout, TimeUnit.MILLISECONDS).retryOnConnectionFailure(false).build()).addConverterFactory(GsonConverterFactory.create()).build().create(CatalogApi::class.java)
        return CatalogRemoteDataSource(api)
    }
    private fun book() = CatalogBookDto("book_1", "Химия", "Мария Белова", 100, 20, null, "organic", RatingDto(4.5, 8), emptyList(), false, false, 0)
    private fun page() = CatalogPageDto(listOf(book()), null, 1)
    private fun enqueue(value: Any) { server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody(gson.toJson(value))) }
    @Test fun `каталог передаёт категорию нормализованный поиск сортировку курсор и локаль`() = runBlocking {
        enqueue(page()); assertTrue(remote().books(CatalogQuery("organic", " Химия   молекул ", CatalogSort.Popular), "opaque") is CatalogResult.Success)
        val request = server.takeRequest(); assertEquals("GET", request.method); assertEquals("/api/v1/catalog/books", request.requestUrl!!.encodedPath)
        assertEquals("organic", request.requestUrl!!.queryParameter("category_id")); assertEquals("Химия молекул", request.requestUrl!!.queryParameter("query"))
        assertEquals("popular", request.requestUrl!!.queryParameter("sort")); assertEquals("opaque", request.requestUrl!!.queryParameter("cursor")); assertEquals("20", request.requestUrl!!.queryParameter("limit")); assertEquals("ru", request.getHeader("Accept-Language"))
    }
    @Test fun `одна буква и слишком длинный запрос не отправляются`() = runBlocking {
        for (query in listOf("Я", "я".repeat(101))) assertEquals(CatalogResult.Failure(CatalogError.InvalidResponse), remote().books(CatalogQuery(search = query), null))
        assertEquals(0, server.requestCount)
    }
    @Test fun `пустые и повреждённые обязательные поля не проходят в представление`() = runBlocking {
        for (bad in listOf(page().copy(items = null), page().copy(items = listOf(book().copy(pagesRead = 101))), page().copy(items = listOf(book().copy(rating = RatingDto(8.0, 2)))))) {
            enqueue(bad); assertEquals(CatalogResult.Failure(CatalogError.InvalidResponse), remote().books(CatalogQuery(), null))
        }
    }
    @Test fun `прочитать все отправляет PUT с границей снимка`() = runBlocking {
        enqueue(NotificationReceiptDto("2026-10-18T12:00:00Z", UnreadDto(2, 8)))
        assertTrue(remote().markAllRead(42) is CatalogResult.Success)
        val request = server.takeRequest(); assertEquals("PUT", request.method); assertEquals("/api/v1/users/me/notifications/read-state", request.path)
        assertEquals(42, JsonParser.parseString(request.body.readUtf8()).asJsonObject.get("through_sequence").asInt)
    }
    @Test fun `отметка одного уведомления идемпотентна и кодируется в пути`() = runBlocking {
        enqueue(NotificationReceiptDto("2026-10-18T12:00:00Z", UnreadDto(2, 8)))
        remote().markRead("note_1")
        val request = server.takeRequest(); assertEquals("PUT", request.method); assertEquals("/api/v1/users/me/notifications/note_1/read", request.path)
    }
    @Test fun `ошибки сессии курсора ограничения сервера и отсутствующего объекта различаются`() = runBlocking {
        for ((status, error) in listOf(401 to CatalogError.Unauthorized, 404 to CatalogError.NotFound, 409 to CatalogError.Conflict, 429 to CatalogError.RateLimited, 503 to CatalogError.Unavailable)) {
            server.enqueue(MockResponse().setResponseCode(status)); assertEquals(CatalogResult.Failure(error), remote().books(CatalogQuery(), null))
        }
    }
    @Test fun `тайм-аут отличается от неправильного JSON`() = runBlocking {
        server.enqueue(MockResponse().setBody(gson.toJson(page())).setBodyDelay(250, TimeUnit.MILLISECONDS)); assertEquals(CatalogResult.Failure(CatalogError.Timeout), remote(40).books(CatalogQuery(), null))
        server.enqueue(MockResponse().setBody("invalid json")); assertEquals(CatalogResult.Failure(CatalogError.InvalidResponse), remote().books(CatalogQuery(), null))
    }
    @Test fun `неизвестные будущие поля не ломают корректный ответ`() = runBlocking {
        val json = gson.toJsonTree(page()).asJsonObject.apply { addProperty("future_field", "value") }
        enqueue(json); assertTrue(remote().books(CatalogQuery(), null) is CatalogResult.Success)
    }
    @Test fun `неверная дата и два адреса перехода в уведомлении отклоняются`() = runBlocking {
        enqueue(NotificationPageDto(listOf(NotificationDto("n", 1, "Название", "Текст", "2026-10-18T12:00:00Z", null, "book", "category")), null, 1, UnreadDto(1, 0)))
        assertEquals(CatalogResult.Failure(CatalogError.InvalidResponse), remote().notifications(NotificationFilter.All, null))
        enqueue(NotificationPageDto(listOf(NotificationDto("n", 1, "Название", "Текст", "yesterday")), null, 1, UnreadDto(1, 0)))
        assertEquals(CatalogResult.Failure(CatalogError.InvalidResponse), remote().notifications(NotificationFilter.All, null))
    }
    @Test fun `агрегат главной ограничивает продолжение чтения пятью книгами`() = runBlocking {
        enqueue(HomeFeedDto((1..7).map { book().copy(id = "book_$it") }, emptyList(), emptyList(), 7, 0, UnreadDto(0, 1)))
        val result = remote().home() as CatalogResult.Success
        assertEquals(listOf("book_1", "book_2", "book_3", "book_4", "book_5"), result.value.continueReading.map { it.id })
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/v1/users/me/home", request.path)
    }

}
