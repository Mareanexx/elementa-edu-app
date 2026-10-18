package ru.mareanexx.reader_impl

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import okio.Buffer
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.book_api.domain.model.BookShelves
import ru.mareanexx.reader_impl.data.remote.model.ReadingManifest
import ru.mareanexx.reader_impl.data.remote.source.PublicationDownloader
import ru.mareanexx.reader_impl.data.epub.ReaderFileException
import java.security.MessageDigest

class PublicationDownloaderTest {
    @get:Rule val files = TemporaryFolder()
    private val server = MockWebServer()
    private val bytes = ByteArray(100000) { (it % 251).toByte() }
    @Before fun start() = server.start()
    @After fun stop() = server.shutdown()
    private fun manifest() = ReadingManifest(ReaderBook("book", "Книга", "Автор", 100, 0, null, "chemistry", null, 0, emptyList(), BookShelves(false, false, 0)), "1", server.url("/content").toString(), bytes.size.toLong(), MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) })
    @Test fun resumesVerifiedRangeAndDoesNotSendAuthorizationToFileStorage() = runBlocking {
        val partial = files.newFile().apply { writeBytes(bytes.copyOfRange(0, 40000)) }
        server.enqueue(MockResponse().setResponseCode(206).setHeader("Content-Range", "bytes 40000-99999/100000").setBody(Buffer().write(bytes.copyOfRange(40000, bytes.size))))
        PublicationDownloader(OkHttpClient()).download(manifest(), partial) { _, _ -> }
        assertArrayEquals(bytes, partial.readBytes())
        val request = server.takeRequest()
        assertEquals("bytes=40000-", request.getHeader("Range")); assertNull(request.getHeader("Authorization"))
    }
    @Test fun restartsWhenServerIgnoresRange() = runBlocking {
        val partial = files.newFile().apply { writeBytes(bytes.copyOfRange(0, 10)) }
        server.enqueue(MockResponse().setBody(Buffer().write(bytes)))
        PublicationDownloader(OkHttpClient()).download(manifest(), partial) { _, _ -> }
        assertArrayEquals(bytes, partial.readBytes())
    }
    @Test fun corruptDigestNeverBecomesReadyFile() = runBlocking {
        val partial = files.newFile()
        server.enqueue(MockResponse().setBody(Buffer().write(ByteArray(bytes.size))))
        val error = runCatching { PublicationDownloader(OkHttpClient()).download(manifest(), partial) { _, _ -> } }.exceptionOrNull() as ReaderFileException
        assertEquals(ReaderError.CorruptBook, error.error); assertFalse(partial.exists())
    }
    @Test fun rejectsWrongContentRange() = runBlocking {
        val partial = files.newFile().apply { writeBytes(bytes.copyOfRange(0, 10)) }
        server.enqueue(MockResponse().setResponseCode(206).setHeader("Content-Range", "bytes 0-99999/100000").setBody(Buffer().write(bytes)))
        val error = runCatching { PublicationDownloader(OkHttpClient()).download(manifest(), partial) { _, _ -> } }.exceptionOrNull() as ReaderFileException
        assertEquals(ReaderError.InvalidResponse, error.error)
        assertEquals(10, partial.length())
    }
    @Test fun completeVerifiedPartialNeedsNoNetwork() = runBlocking {
        val partial = files.newFile().apply { writeBytes(bytes) }
        PublicationDownloader(OkHttpClient()).download(manifest(), partial) { _, _ -> }
        assertEquals(0, server.requestCount)
    }
}
