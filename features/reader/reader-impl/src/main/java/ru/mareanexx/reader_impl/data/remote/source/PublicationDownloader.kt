package ru.mareanexx.reader_impl.data.remote.source

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.mareanexx.reader_api.domain.model.ReaderError
import ru.mareanexx.reader_impl.data.epub.ReaderFileException
import ru.mareanexx.reader_impl.data.remote.model.ReadingManifest
import java.io.File
import java.io.IOException
import java.security.MessageDigest

/** Частичный файл никогда не считается готовой книгой. Возобновление проверяет диапазон и SHA-256. */
internal class PublicationDownloader(
    private val client: OkHttpClient,
) {
    suspend fun download(manifest: ReadingManifest, partial: File, progress: (Long, Long) -> Unit) = runInterruptible(Dispatchers.IO) {
        partial.parentFile!!.mkdirs()
        var offset = partial.length().takeIf { it in 1 until manifest.byteSize } ?: 0L
        if (partial.length() == manifest.byteSize && digest(partial) == manifest.sha256) return@runInterruptible
        val request = Request.Builder().url(manifest.contentUrl).header("Accept-Encoding", "identity")
            .apply { if (offset > 0) header("Range", "bytes=$offset-") }.build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ReaderFileException(ReaderRemoteDataSource.httpError(response.code))
            if (offset > 0 && response.code == 206) {
                if (response.header("Content-Range") != "bytes $offset-${manifest.byteSize - 1}/${manifest.byteSize}") throw ReaderFileException(ReaderError.InvalidResponse)
            } else if (response.code == 200) offset = 0
            else if (response.code != 206 || offset == 0L) throw ReaderFileException(ReaderError.InvalidResponse)
            val body = response.body ?: throw ReaderFileException(ReaderError.InvalidResponse)
            if (body.contentLength() > manifest.byteSize - offset) throw ReaderFileException(ReaderError.CorruptBook)
            var written = offset
            var reported = 0L
            java.io.FileOutputStream(partial, offset > 0).use { sink ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(32 * 1024)
                    while (true) {
                        if (Thread.currentThread().isInterrupted) throw java.io.InterruptedIOException()
                        val count = input.read(buffer)
                        if (count < 0) break
                        written += count
                        if (written > manifest.byteSize) throw ReaderFileException(ReaderError.CorruptBook)
                        sink.write(buffer, 0, count)
                        val now = System.nanoTime()
                        if (now - reported > 200_000_000 || written == manifest.byteSize) {
                            progress(written, manifest.byteSize)
                            reported = now
                        }
                    }
                    sink.fd.sync()
                }
            }
            if (written != manifest.byteSize) throw IOException("Неполная загрузка книги")
            if (digest(partial) != manifest.sha256) {
                partial.delete()
                throw ReaderFileException(ReaderError.CorruptBook)
            }
        }
    }
    companion object {
        fun digest(file: File): String {
            val sha = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input -> val buffer = ByteArray(64 * 1024); while (true) { val count = input.read(buffer); if (count < 0) break; sha.update(buffer, 0, count) } }
            return sha.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
