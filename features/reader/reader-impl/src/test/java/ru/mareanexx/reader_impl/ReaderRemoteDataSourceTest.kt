package ru.mareanexx.reader_impl

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_impl.data.remote.api.ReaderApi
import ru.mareanexx.reader_impl.data.remote.source.ReaderRemoteDataSource

class ReaderRemoteDataSourceTest {
    private val server = MockWebServer()
    private lateinit var source: ReaderRemoteDataSource
    @Before fun start() {
        server.start()
        val api = Retrofit.Builder().baseUrl(server.url("/")).client(OkHttpClient()).addConverterFactory(GsonConverterFactory.create()).build().create(ReaderApi::class.java)
        source = ReaderRemoteDataSource(api, server.url("/").toString(), "device-test")
    }
    @After fun stop() = server.shutdown()
    @Test fun preventsExternalFileUrlAndInvalidIntegrityMetadata() = runBlocking {
        val book = """{"id":"b","title":"Книга","authors":"Автор","pages":100,"pages_read":0,"rating_count":0,"favorite":false,"want_to_read":false,"collection_count":0}"""
        server.enqueue(MockResponse().setBody("""{"book":$book,"format":"application/epub+zip","version":"1","content_path":"//evil.test/api/v1/content","byte_size":100,"sha256":"${"a".repeat(64)}"}"""))
        assertEquals(ReaderResult.Failure(ReaderError.InvalidResponse), source.manifest("b"))
    }
    @Test fun missingProgressAndAuthorizationAreDistinctFailures() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404)); server.enqueue(MockResponse().setResponseCode(401))
        assertEquals(ReaderResult.Failure(ReaderError.NotFound), source.progress("b"))
        assertEquals(ReaderResult.Failure(ReaderError.Unauthorized), source.progress("b"))
    }
    @Test fun rejectsMismatchedQuoteOffsetsInRemoteAnnotation() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"items":[{"id":"id","book_id":"b","anchor":{"version":"1","chapter_id":"c","start":0,"end":10,"quote":"abc"},"color":"mint","created_at":1,"updated_at":1,"revision":1}]}"""))
        assertEquals(ReaderResult.Failure(ReaderError.InvalidResponse), source.annotations("b", null))
    }
}
