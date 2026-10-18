package ru.mareanexx.user_library_impl.data.mock

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import ru.mareanexx.collections_api.domain.model.CollectionBook
import ru.mareanexx.collections_impl.data.mock.DemoBookLibraryBridge
import ru.mareanexx.user_library_api.domain.validation.LibrarySearchRules
import ru.mareanexx.user_library_impl.data.remote.dto.*
import java.io.IOException
import java.text.Collator
import java.text.Normalizer
import java.util.Base64
import java.util.Locale

/** Общие связи и прогресс видны библиотеке, коллекциям и экрану книги. */
internal class DemoLibraryInterceptor(
    context: Context,
    private val userId: String,
) : Interceptor {
    private val bridge = DemoBookLibraryBridge(context, userId)
    private val store = DemoLibraryStore(context, userId)
    private val controls = context.getSharedPreferences("library_mock_controls", Context.MODE_PRIVATE)
    private val gson = Gson()
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val base = "/api/v1/users/me/library"
        if (request.url.encodedPath != base && !request.url.encodedPath.startsWith("$base/")) return chain.proceed(request)
        Thread.sleep(controls.getInt("delay_ms", 300).coerceIn(0, 5000).toLong())
        if (!request.header("Authorization").orEmpty().startsWith("Bearer ")) return error(request, 401, "UNAUTHORIZED")
        return synchronized(DemoLibraryStore.lock) {
            val forced = controls.getInt("next_status", 0)
            if (forced != 0) controls.edit().remove("next_status").commit()
            if (forced == -1) throw IOException("Демонстрационная ошибка подключения")
            if (forced > 0) return@synchronized error(request, forced, "MOCK_ERROR")
            try {
                val path = request.url.encodedPath.removePrefix(base).trim('/')
                when {
                    path == "preferences" -> preferences(request)
                    path.isEmpty() && request.method == "GET" -> books(request)
                    path.startsWith("books/") && path.endsWith("/reading-status") && request.method == "PUT" -> status(request, request.url.pathSegments[6])
                    else -> error(request, 405, "METHOD_NOT_ALLOWED")
                }
            } catch (failed: IOException) { throw failed
            } catch (_: RuntimeException) { error(request, 422, "INVALID_REQUEST") }
        }
    }
    private fun preferences(request: Request): Response {
        if (request.method == "PATCH") {
            val sort = gson.fromJson(body(request), LibraryPreferencesDto::class.java).sort
            if (sort !in listOf("recent", "title_asc", "author_asc")) return error(request, 422, "INVALID_PREFERENCES")
            store.saveSort(requireNotNull(sort))
        } else if (request.method != "GET") return error(request, 405, "METHOD_NOT_ALLOWED")
        return json(request, 200, LibraryPreferencesDto(store.sort()))
    }
    private fun books(request: Request): Response {
        val status = request.url.queryParameter("status") ?: "all"
        val sort = request.url.queryParameter("sort") ?: store.sort()
        val query = LibrarySearchRules.normalize(request.url.queryParameter("query").orEmpty())
        if (status !in listOf("all", "in_progress", "completed", "want_to_read") || sort !in listOf("recent", "title_asc", "author_asc") || !LibrarySearchRules.canSearch(query)) return error(request, 422, "INVALID_QUERY")
        if (query.isNotEmpty()) controls.edit().putInt("search_requests", controls.getInt("search_requests", 0) + 1).putString("last_query", query).commit()
        val wanted = bridge.collections().firstOrNull { it.isReadingList }?.id
        val tokens = fold(query).split(' ').filter(String::isNotBlank)
        val available = if (controls.getBoolean("empty_library", false)) emptyList() else bridge.books()
        val matched = available.filter { book ->
            val text = fold(listOf(book.title, book.authors).plus(book.tags.map { it.label }).joinToString(" "))
            tokens.all(text::contains)
        }
        fun planned(book: CollectionBook) = wanted != null && bridge.contains(wanted, book.id)
        val counts = LibraryCountsDto(matched.size, matched.count { it.pagesRead in 1 until it.pages }, matched.count { it.pagesRead == it.pages }, matched.count(::planned))
        val filtered = matched.filter { book -> when (status) {
            "in_progress" -> book.pagesRead in 1 until book.pages
            "completed" -> book.pagesRead == book.pages
            "want_to_read" -> planned(book)
            else -> true
        } }
        val collator = Collator.getInstance(Locale.forLanguageTag("ru")).apply { strength = Collator.PRIMARY }
        val ordered = when (sort) {
            "title_asc" -> filtered.sortedWith { a, b -> collator.compare(a.title, b.title).takeUnless { it == 0 } ?: a.id.compareTo(b.id) }
            "author_asc" -> filtered.sortedWith { a, b -> collator.compare(a.authors, b.authors).takeUnless { it == 0 } ?: collator.compare(a.title, b.title).takeUnless { it == 0 } ?: a.id.compareTo(b.id) }
            else -> filtered.sortedWith(compareByDescending<CollectionBook> { store.touched(it.id, 100L - it.id.removePrefix("book_").toLong()) }.thenBy { it.id })
        }
        val scope = listOf(userId, status, sort, query)
        val raw = request.url.queryParameter("cursor")
        val offset = if (raw == null) 0 else {
            val cursor = runCatching { gson.fromJson(String(Base64.getUrlDecoder().decode(raw)), Array<String>::class.java).toList() }.getOrNull()
            if (cursor == null || cursor.size != 5 || cursor.take(4) != scope) return error(request, 409, "CURSOR_EXPIRED")
            val last = ordered.indexOfFirst { it.id == cursor[4] }
            if (last < 0) return error(request, 409, "CURSOR_EXPIRED")
            last + 1
        }
        val limit = minOf(request.url.queryParameter("limit")?.toIntOrNull()?.coerceIn(1, 50) ?: 20, controls.getInt("page_size", 50).coerceIn(1, 50))
        val page = ordered.drop(offset).take(limit)
        val next = page.lastOrNull()?.takeIf { offset + page.size < ordered.size }?.let {
            Base64.getUrlEncoder().withoutPadding().encodeToString(gson.toJson(scope + it.id).toByteArray())
        }
        val highlights = if (raw == null && query.isEmpty() && status == "all") LibraryHighlightsDto(
            matched.filter { it.pagesRead in 1 until it.pages }.take(2).map(::dto),
            matched.filter { it.pagesRead == it.pages }.take(8).map(::dto),
            matched.filter(::planned).take(8).map(::dto),
        ) else null
        return json(request, 200, LibraryPageDto(page.map(::dto), next, ordered.size, counts, highlights))
    }
    private fun status(request: Request, id: String): Response {
        val book = bridge.book(id) ?: return error(request, 404, "BOOK_NOT_FOUND")
        val status = gson.fromJson(body(request), LibraryStatusRequest::class.java).status
        if (status !in listOf("completed", "not_started")) return error(request, 422, "INVALID_READING_STATUS")
        bridge.setPagesRead(id, if (status == "completed") book.pages else 0)
        store.touch(id)
        return json(request, 200, dto(requireNotNull(bridge.book(id))))
    }
    private fun dto(book: CollectionBook): LibraryBookDto {
        val all = bridge.collections()
        return LibraryBookDto(
            book.id, book.title, book.authors, book.pages, book.pagesRead,
            when (book.pagesRead) { 0 -> "not_started"; book.pages -> "completed"; else -> "in_progress" },
            book.coverUrl, book.cover.name.lowercase(Locale.ROOT), book.rating?.let { LibraryRatingDto(it.average, it.count) },
            book.tags.map { LibraryTagDto(it.id, it.label, it.kind.name.lowercase(Locale.ROOT)) },
            all.any { it.isFavorite && bridge.contains(it.id, book.id) }, all.any { it.isReadingList && bridge.contains(it.id, book.id) },
            all.count { !it.isSystem && bridge.contains(it.id, book.id) },
        )
    }
    private fun fold(value: String) = Normalizer.normalize(value, Normalizer.Form.NFKC).lowercase(Locale.ROOT).replace('ё', 'е')
    private fun body(request: Request) = Buffer().also { request.body?.writeTo(it) }.readUtf8()
    private fun error(request: Request, status: Int, code: String) = json(request, status, mapOf("error" to mapOf("code" to code)))
    private fun json(request: Request, status: Int, value: Any) = Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(status)
        .message("Демонстрационный ответ").body(gson.toJson(value).toResponseBody("application/json".toMediaType())).build()
}
