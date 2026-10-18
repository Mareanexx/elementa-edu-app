package ru.mareanexx.reader_impl.data.mock

import android.content.Context
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import ru.mareanexx.collections_impl.data.mock.DemoBookLibraryBridge
import ru.mareanexx.reader_impl.data.remote.dto.*
import ru.mareanexx.reader_impl.data.remote.mapper.ReaderMapper
import java.security.MessageDigest

internal class DemoReaderInterceptor(
    context: Context,
    private val userId: String,
) : Interceptor {
    private val library = DemoBookLibraryBridge(context, userId)
    private val store = context.getSharedPreferences("reader_demo_server", Context.MODE_PRIVATE)
    private val controls = context.getSharedPreferences("reader_mock_controls", Context.MODE_PRIVATE)
    private val gson = Gson()
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.pathSegments
        if (path.take(2) != listOf("api", "v1")) return chain.proceed(request)
        val isContent = path.size == 5 && path[2] == "reading-content"
        val isManifest = path.size == 5 && path[2] == "books" && path[4] == "reading-manifest"
        val isReading = path.size >= 8 && path.subList(2, 6) == listOf("users", "me", "reading", "books")
        if (!isContent && !isManifest && !isReading) return chain.proceed(request)
        fun response(code: Int, value: Any? = null) = Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(code).message("Demo reader")
            .body((if (value == null) "" else gson.toJson(value)).toResponseBody("application/json".toMediaType())).build()
        if (!isContent && request.header("Authorization").isNullOrBlank()) return response(401)
        val delay = controls.getLong(if (isContent) "download_delay_ms" else "delay_ms", if (isContent) 600 else 180)
        if (delay > 0) Thread.sleep(delay.coerceAtMost(10000))
        val forced = controls.getInt("next_status", 0)
        if (forced > 0) { controls.edit().remove("next_status").commit(); return response(forced) }
        val id = path[if (isReading) 6 else 3]
        val book = library.book(id) ?: return response(404)
        val bytes = DemoEpub.bytes(id, book.title)
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        if (isContent) {
            if (request.url.queryParameter("ticket") != "$userId-$hash") return response(403)
            val offset = request.header("Range")?.removePrefix("bytes=")?.removeSuffix("-")?.toIntOrNull() ?: 0
            if (offset !in 0 until bytes.size) return response(416)
            val payload = bytes.copyOfRange(offset, bytes.size)
            if (controls.getBoolean("corrupt", false)) payload[payload.lastIndex] = (payload.last().toInt() xor 1).toByte()
            return response(if (offset > 0) 206 else 200).newBuilder().apply {
                if (offset > 0) header("Content-Range", "bytes $offset-${bytes.lastIndex}/${bytes.size}")
            }.body(payload.toResponseBody("application/epub+zip".toMediaType())).build()
        }
        if (isManifest) {
            val collections = library.collections()
            val summary = ReaderBookDto(id, book.title, book.authors, book.pages, book.pagesRead, book.coverUrl, book.cover.name.lowercase(),
                book.rating?.average, book.rating?.count ?: 0, book.tags.map { ReaderTagDto(it.id, it.label, it.kind.name.lowercase()) },
                collections.any { it.isFavorite && library.contains(it.id, id) }, collections.any { it.isReadingList && library.contains(it.id, id) },
                collections.count { !it.isFavorite && !it.isReadingList && library.contains(it.id, id) })
            return response(200, ReadingManifestDto(summary, "application/epub+zip", "demo-epub-1", "/api/v1/reading-content/$id/book.epub?ticket=$userId-$hash", bytes.size.toLong(), hash))
        }
        return synchronized(lock) {
            val prefix = "${userId}_${id}"
            val body = Buffer().also { request.body?.writeTo(it) }.readUtf8()
            if (path[7] == "progress") {
                if (request.method == "GET") return@synchronized store.getString("position_$prefix", null)?.let { response(200, gson.fromJson(it, ReaderPositionDto::class.java)) } ?: response(404)
                if (request.method == "PUT") {
                    val input = runCatching { gson.fromJson(body, ReaderProgressRequest::class.java) }.getOrNull() ?: return@synchronized response(400)
                    val value = runCatching { ReaderMapper.position(input.position) }.getOrNull() ?: return@synchronized response(422)
                    if (value.bookId != id || value.sourcePagesRead !in 0..book.pages) return@synchronized response(422)
                    val key = "revision_${prefix}_${input.deviceId}"
                    if (input.clientRevision > store.getLong(key, 0)) {
                        check(store.edit().putString("position_$prefix", gson.toJson(input.position)).putLong(key, input.clientRevision).commit())
                        library.setPagesRead(id, value.sourcePagesRead)
                    }
                    return@synchronized response(200, input.position)
                }
            }
            if (path[7] == "annotations") {
                val key = "annotations_$prefix"
                val records = store.getString(key, null)?.let { gson.fromJson(it, Array<ReaderAnnotationDto>::class.java).toMutableList() } ?: mutableListOf()
                if (request.method == "GET") return@synchronized response(200, ReaderAnnotationsPageDto(records, null))
                val annotationId = path.getOrNull(8) ?: return@synchronized response(400)
                val old = records.firstOrNull { it.id == annotationId }
                if (request.method == "DELETE") {
                    if (old == null) return@synchronized response(404)
                    records.remove(old); records += old.copy(deleted = true, updatedAt = System.currentTimeMillis(), revision = (old.revision ?: 0) + 1)
                    check(store.edit().putString(key, gson.toJson(records)).commit())
                    return@synchronized response(204)
                }
                if (request.method == "PUT") {
                    val dto = runCatching { gson.fromJson(body, ReaderAnnotationDto::class.java) }.getOrNull() ?: return@synchronized response(400)
                    if (runCatching { ReaderMapper.annotation(dto) }.isFailure || dto.id != annotationId || dto.bookId != id) return@synchronized response(422)
                    if (old == null || (dto.updatedAt ?: 0) >= (old.updatedAt ?: 0)) { records.removeAll { it.id == annotationId }; records += dto; check(store.edit().putString(key, gson.toJson(records)).commit()) }
                    return@synchronized response(200, dto)
                }
            }
            response(405)
        }
    }
    private companion object { val lock = Any() }
}
