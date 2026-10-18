package ru.mareanexx.catalog_impl.data.mock

import android.content.Context
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import ru.mareanexx.catalog_api.domain.validation.CatalogSearchRules
import ru.mareanexx.catalog_impl.data.remote.dto.*
import ru.mareanexx.collections_api.domain.model.CollectionBook
import ru.mareanexx.collections_impl.data.mock.DemoBookLibraryBridge
import java.io.IOException
import java.text.Collator
import java.util.Base64
import java.util.Locale

/** Мок находится перед Retrofit и использует общие с библиотекой данные книг. */
internal class DemoCatalogInterceptor(
    context: Context,
    private val userId: String,
) : Interceptor {
    private val bridge = DemoBookLibraryBridge(context, userId)
    private val notifications = DemoNotificationsStore(context, userId)
    private val controls = context.getSharedPreferences("catalog_mock_controls", Context.MODE_PRIVATE)
    private val gson = Gson()
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        if (!path.startsWith("/api/v1/catalog/") && path != HOME && !path.startsWith(NOTICES)) return chain.proceed(request)
        Thread.sleep(controls.getInt("delay_ms", 280).coerceIn(0, 5000).toLong())
        if (!request.header("Authorization").orEmpty().startsWith("Bearer ")) return error(request, 401)
        return synchronized(DemoNotificationsStore.lock) {
            val forced = controls.getInt("next_status", 0)
            if (forced != 0) controls.edit().remove("next_status").commit()
            if (forced == -1) throw IOException("Демонстрационная ошибка подключения")
            if (forced > 0) return@synchronized error(request, forced)
            try {
                when {
                    path == HOME && request.method == "GET" -> home(request)
                    path == "/api/v1/catalog/categories" && request.method == "GET" -> json(request, 200, DemoCatalogCategories.values(empty()))
                    path == "/api/v1/catalog/books" && request.method == "GET" -> books(request)
                    path == "/api/v1/catalog/search/suggestions" && request.method == "GET" -> json(request, 200, SuggestionsDto(listOf("Органическая химия", "Космос", "Лабораторный практикум", "Биохимия")))
                    path == NOTICES && request.method == "GET" -> inbox(request)
                    path == "$NOTICES/read-state" && request.method == "PUT" -> json(request, 200, notifications.readThrough(gson.fromJson(body(request), ReadStateRequest::class.java).throughSequence))
                    path.startsWith("$NOTICES/") && path.endsWith("/read") && request.method == "PUT" -> {
                        val id = path.removePrefix("$NOTICES/").removeSuffix("/read")
                        if (notifications.items().none { it.id == id }) error(request, 404) else json(request, 200, notifications.read(id))
                    }
                    else -> error(request, 405)
                }
            } catch (failed: IOException) { throw failed
            } catch (_: RuntimeException) { error(request, 422) }
        }
    }
    private fun empty() = controls.getBoolean("empty_catalog", false)
    private fun available() = if (empty()) emptyList() else bridge.books()
    private fun home(request: Request): Response {
        val books = available()
        val popular = books.sortedByDescending { it.rating?.count ?: 0 }.take(6)
        return json(request, 200, HomeFeedDto(books.filter { it.pagesRead in 1 until it.pages }.take(5).map(::dto),
            DemoCatalogCategories.values(empty()), listOf(
                HomeSectionDto("popular", popular.map(::dto), "popular"),
                HomeSectionDto("recommended", books.filter { it.pagesRead == 0 }.take(6).map(::dto), "recommended"),
                HomeSectionDto("newest", books.reversed().take(6).map(::dto), "newest")),
            bridge.books().size, bridge.collections().count { !it.isSystem }, notifications.unread()))
    }
    private fun books(request: Request): Response {
        val category = request.url.queryParameter("category_id").orEmpty()
        if (category.isNotEmpty() && category !in DemoCatalogCategories.membership) return error(request, 404)
        val query = CatalogSearchRules.normalize(request.url.queryParameter("query").orEmpty())
        val sort = request.url.queryParameter("sort") ?: "recommended"
        if ((query.isNotEmpty() && !CatalogSearchRules.valid(query)) || sort !in listOf("recommended", "popular", "newest", "title_asc", "relevance")) return error(request, 422)
        if (query.isNotEmpty()) controls.edit().putInt("search_requests", controls.getInt("search_requests", 0) + 1).putString("last_query", query).commit()
        val books = available().filter { book ->
            val categoryText = DemoCatalogCategories.values().filter { book.id in DemoCatalogCategories.membership.getValue(requireNotNull(it.id)) }.joinToString(" ") { it.title.orEmpty() }
            val fields = listOf(book.title, book.authors, categoryText).plus(book.tags.map { it.label })
            (category.isEmpty() || book.id in DemoCatalogCategories.membership.getValue(category)) && CatalogSearchRules.matches(query, fields)
        }
        val ordered = when (sort) {
            "title_asc" -> {
                val collator = Collator.getInstance(Locale.forLanguageTag("ru")).apply { strength = Collator.PRIMARY }
                books.sortedWith { a, b -> collator.compare(a.title, b.title).takeUnless { it == 0 } ?: a.id.compareTo(b.id) }
            }
            "popular" -> books.sortedWith(compareByDescending<CollectionBook> { it.rating?.count ?: 0 }.thenBy { it.id })
            "newest" -> books.reversed()
            "relevance" -> books.sortedByDescending { if (CatalogSearchRules.key(it.title).startsWith(CatalogSearchRules.key(query))) 1 else 0 }
            else -> books
        }
        val scope = listOf(userId, category, query, sort)
        val raw = request.url.queryParameter("cursor")
        val cursor = if (raw == null) DemoCatalogCursor(scope, ordered.map { it.id }, 0) else runCatching { decode(raw, DemoCatalogCursor::class.java) }.getOrNull()
        if (cursor == null || cursor.scope != scope || cursor.offset !in 0..cursor.ids.size || cursor.ids.toSet() != ordered.map { it.id }.toSet()) return error(request, 409)
        val limit = minOf(request.url.queryParameter("limit")?.toIntOrNull()?.coerceIn(1, 50) ?: 20, controls.getInt("page_size", 50).coerceIn(1, 50))
        val ids = cursor.ids.drop(cursor.offset).take(limit)
        val byId = ordered.associateBy { it.id }
        val offset = cursor.offset + ids.size
        val next = if (offset < cursor.ids.size) encode(cursor.copy(offset = offset)) else null
        return json(request, 200, CatalogPageDto(ids.map { dto(byId.getValue(it)) }, next, ordered.size))
    }
    private fun inbox(request: Request): Response {
        val filter = request.url.queryParameter("filter") ?: "all"
        if (filter !in listOf("all", "unread")) return error(request, 422)
        val raw = request.url.queryParameter("cursor")
        val cursor = if (raw == null) DemoNotificationCursor(userId, filter, 6, 7) else runCatching { decode(raw, DemoNotificationCursor::class.java) }.getOrNull()
        if (cursor == null || cursor.owner != userId || cursor.filter != filter || cursor.snapshot !in 0..6 || cursor.before !in 1..7) return error(request, 409)
        val all = (if (controls.getBoolean("empty_notifications", false)) emptyList() else notifications.items()).filter { requireNotNull(it.sequence) <= cursor.snapshot && it.sequence < cursor.before && (filter == "all" || it.readAt == null) }
        val limit = minOf(request.url.queryParameter("limit")?.toIntOrNull()?.coerceIn(1, 50) ?: 20, controls.getInt("page_size", 50).coerceIn(1, 50))
        val page = all.take(limit)
        val next = if (page.size < all.size) encode(cursor.copy(before = requireNotNull(page.last().sequence))) else null
        return json(request, 200, NotificationPageDto(page, next, cursor.snapshot, notifications.unread()))
    }
    private fun dto(book: CollectionBook): CatalogBookDto {
        val collections = bridge.collections()
        return CatalogBookDto(book.id, book.title, book.authors, book.pages, book.pagesRead, book.coverUrl, book.cover.name.lowercase(Locale.ROOT),
            book.rating?.let { RatingDto(it.average, it.count) }, book.tags.map { TagDto(it.id, it.label, it.kind.name.lowercase(Locale.ROOT)) },
            collections.any { it.isFavorite && bridge.contains(it.id, book.id) }, collections.any { it.isReadingList && bridge.contains(it.id, book.id) }, collections.count { !it.isSystem && bridge.contains(it.id, book.id) })
    }
    private fun encode(value: Any) = Base64.getUrlEncoder().withoutPadding().encodeToString(gson.toJson(value).toByteArray())
    private fun <T> decode(value: String, type: Class<T>): T = gson.fromJson(String(Base64.getUrlDecoder().decode(value)), type)
    private fun body(request: Request) = Buffer().also { request.body?.writeTo(it) }.readUtf8()
    private fun error(request: Request, status: Int) = json(request, status, mapOf("error" to mapOf("code" to "CATALOG_$status")))
    private fun json(request: Request, status: Int, value: Any) = Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(status)
        .message("Демонстрационный ответ").body(gson.toJson(value).toResponseBody("application/json".toMediaType())).build()
    companion object {
        private const val HOME = "/api/v1/users/me/home"
        private const val NOTICES = "/api/v1/users/me/notifications"
    }
}
