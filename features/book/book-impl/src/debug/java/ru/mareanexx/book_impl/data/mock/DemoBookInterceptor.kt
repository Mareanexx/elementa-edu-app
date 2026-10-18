package ru.mareanexx.book_impl.data.mock

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import ru.mareanexx.book_api.domain.model.ReviewInput
import ru.mareanexx.book_api.domain.validation.BookInputRules
import ru.mareanexx.book_impl.R
import ru.mareanexx.book_impl.data.remote.dto.*
import ru.mareanexx.collections_impl.data.mock.DemoBookLibraryBridge
import java.io.IOException
import java.time.Instant
import java.util.UUID

internal class DemoBookInterceptor(
    private val context: Context,
    private val userId: String,
    private val fullName: String,
) : Interceptor {
    private val library = DemoBookLibraryBridge(context, userId)
    private val store = DemoBookStore(context)
    private val controls = context.getSharedPreferences("book_mock_controls", Context.MODE_PRIVATE)
    private val gson = GsonBuilder().serializeNulls().create()
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val parts = request.url.pathSegments
        val private = parts.take(5) == listOf("api", "v1", "users", "me", "books")
        if (!private && parts.take(3) != listOf("api", "v1", "books")) return chain.proceed(request)
        Thread.sleep(controls.getInt("delay_ms", 300).coerceIn(0, 5000).toLong())
        if (!request.header("Authorization").orEmpty().startsWith("Bearer ")) return error(request, 401, "UNAUTHORIZED")
        val forced = synchronized(DemoBookStore.lock) {
            controls.getInt("next_status", 0).also { if (it != 0) controls.edit().remove("next_status").commit() }
        }
        if (forced == -1) throw IOException("Демонстрационный сбой подключения")
        if (forced > 0) return error(request, forced, "MOCK_ERROR")
        val route = parts.drop(if (private) 5 else 3)
        val bookId = route.firstOrNull() ?: return error(request, 404, "BOOK_NOT_FOUND")
        return synchronized(DemoBookStore.lock) {
            try {
                val book = library.book(bookId) ?: return@synchronized error(request, 404, "BOOK_NOT_FOUND")
                val reviews = store.reviews(bookId, book.rating?.count ?: 0, book.rating?.average)
                if (private) privateAction(request, route, bookId)
                else when {
                    route.size == 1 && request.method == "GET" -> json(request, 200, details(bookId, reviews))
                    route.getOrNull(1) == "reviews" -> reviewAction(request, route, bookId, reviews)
                    else -> error(request, 405, "METHOD_NOT_ALLOWED")
                }
            } catch (failed: IOException) { throw failed }
            catch (_: IllegalArgumentException) { error(request, 422, "INVALID_REQUEST") }
            catch (_: RuntimeException) { error(request, 500, "MOCK_STORAGE_ERROR") }
        }
    }
    private fun privateAction(request: Request, route: List<String>, bookId: String): Response {
        if (route.size != 2) return error(request, 404, "BOOK_NOT_FOUND")
        return when (route[1]) {
            "favorite", "reading-list" -> {
                if (request.method !in listOf("PUT", "DELETE")) return error(request, 405, "METHOD_NOT_ALLOWED")
                library.setSystem(bookId, route[1] == "reading-list", request.method == "PUT")
                json(request, 200, shelves(bookId))
            }
            "collections" -> when (request.method) {
                "GET" -> json(request, 200, BookCollectionsDto(library.collections().filter { !it.isSystem }.map {
                    BookCollectionChoiceDto(it.id, it.name, it.bookCount, library.contains(it.id, bookId))
                }))
                "PUT" -> {
                    val ids = runCatching { JsonParser.parseString(body(request)).asJsonObject.getAsJsonArray("collection_ids").map { it.asString }.toSet() }.getOrNull()
                        ?: return error(request, 422, "COLLECTION_NOT_FOUND")
                    if (ids.any { id -> library.collections().none { it.id == id && !it.isSystem } }) return error(request, 404, "COLLECTION_NOT_FOUND")
                    library.setCustom(bookId, ids)
                    json(request, 200, shelves(bookId))
                }
                else -> error(request, 405, "METHOD_NOT_ALLOWED")
            }
            else -> error(request, 404, "BOOK_NOT_FOUND")
        }
    }
    private fun reviewAction(request: Request, route: List<String>, bookId: String, records: List<DemoStoredReview>): Response {
        if (route.size == 2 && request.method == "GET") {
            val sort = request.url.queryParameter("sort") ?: "helpful"
            val filter = request.url.queryParameter("rating")?.toIntOrNull()
            if (sort !in listOf("helpful", "newest", "rating_desc", "rating_asc") || filter != null && filter !in 1..5) return error(request, 422, "INVALID_QUERY")
            val filtered = records.filter { filter == null || it.rating == filter }
            val ordered = when (sort) {
                "newest" -> filtered.sortedByDescending { Instant.parse(it.createdAt) }
                "rating_desc" -> filtered.sortedByDescending { it.rating }
                "rating_asc" -> filtered.sortedBy { it.rating }
                else -> filtered.sortedByDescending { item -> item.baseLikes - item.baseDislikes + item.reactions.values.sumOf { if (it == "like") 1 else -1 } }
            }
            val offset = offset(request, ordered.map { it.id }) ?: return error(request, 422, "INVALID_CURSOR")
            val limit = request.url.queryParameter("limit")?.toIntOrNull()?.coerceIn(1, 30) ?: 10
            val page = ordered.drop(offset).take(limit)
            return json(request, 200, BookReviewsPageDto(page.map(::review), page.lastOrNull()?.id?.takeIf { offset + page.size < ordered.size }, ordered.size))
        }
        if (route.getOrNull(2) == "me" && route.size == 3) {
            val mine = records.firstOrNull { it.authorId == userId }
            if (request.method == "DELETE") {
                val remaining = records.filter { it.authorId != userId }
                store.saveReviews(bookId, remaining)
                cacheRating(bookId, remaining)
                return json(request, 204, null)
            }
            if (request.method != "PUT") return error(request, 405, "METHOD_NOT_ALLOWED")
            val form = gson.fromJson(body(request), BookReviewRequest::class.java)
            val text = form.text.orEmpty().trim()
            BookInputRules.review(ReviewInput(form.rating, text))?.let { return error(request, 422, if (form.rating !in 1..5) "RATING_REQUIRED" else "REVIEW_TOO_LONG") }
            val now = Instant.now().toString()
            val author = author()
            val item = mine?.copy(rating = form.rating, text = text, updatedAt = now, authorName = author.first, avatarUrl = author.second)
                ?: DemoStoredReview(UUID.randomUUID().toString(), userId, author.first, author.second, form.rating, text, now, null, 0, 0)
            val updated = if (mine == null) records + item else records.map { if (it.id == mine.id) item else it }
            store.saveReviews(bookId, updated)
            cacheRating(bookId, updated)
            return json(request, 200, review(item))
        }
        val target = records.firstOrNull { it.id == route.getOrNull(2) } ?: return error(request, 404, "REVIEW_NOT_FOUND")
        if (route.size == 4 && route[3] == "reaction" && request.method == "PUT") {
            if (target.authorId == userId) return error(request, 403, "OWN_REVIEW_REACTION")
            val value = gson.fromJson(body(request), BookReactionRequest::class.java).value
            if (value != null && value !in listOf("like", "dislike")) return error(request, 422, "INVALID_REACTION")
            val updated = target.copy(reactions = if (value == null) target.reactions - userId else target.reactions + (userId to value))
            store.saveReviews(bookId, records.map { if (it.id == target.id) updated else it })
            return json(request, 200, review(updated))
        }
        if (route.getOrNull(3) == "replies") return repliesAction(request, route, target)
        return error(request, 405, "METHOD_NOT_ALLOWED")
    }
    private fun repliesAction(request: Request, route: List<String>, target: DemoStoredReview): Response {
        val records = store.replies(target.id).sortedByDescending { Instant.parse(it.createdAt) }
        if (route.size == 4 && request.method == "GET") {
            val offset = offset(request, records.map { it.id }) ?: return error(request, 422, "INVALID_CURSOR")
            val page = records.drop(offset).take(10)
            return json(request, 200, BookRepliesPageDto(page.map(::reply), page.lastOrNull()?.id?.takeIf { offset + page.size < records.size }, records.size))
        }
        if (route.size == 4 && request.method == "POST") {
            val text = gson.fromJson(body(request), BookReplyRequest::class.java).text.orEmpty().trim()
            BookInputRules.reply(text)?.let { return error(request, 422, if (text.isBlank()) "REPLY_REQUIRED" else "REPLY_TOO_LONG") }
            val key = request.header("Idempotency-Key")?.takeIf { runCatching { UUID.fromString(it).toString() == it }.getOrDefault(false) }
                ?: return error(request, 422, "INVALID_IDEMPOTENCY_KEY")
            records.firstOrNull { it.authorId == userId && it.requestKey == key }?.let {
                return if (it.text == text) json(request, 201, reply(it)) else error(request, 409, "IDEMPOTENCY_CONFLICT")
            }
            val author = author()
            val item = DemoStoredReply(UUID.randomUUID().toString(), userId, author.first, author.second, text, Instant.now().toString(), key)
            store.saveReplies(target.id, records + item)
            if (controls.getBoolean("drop_reply_after_save", false)) {
                controls.edit().remove("drop_reply_after_save").commit()
                throw IOException("Демонстрационная потеря ответа после сохранения")
            }
            return json(request, 201, reply(item))
        }
        if (route.size == 5 && request.method == "DELETE") {
            val item = records.firstOrNull { it.id == route[4] } ?: return json(request, 204, null)
            if (item.authorId != userId) return error(request, 403, "REPLY_FORBIDDEN")
            store.saveReplies(target.id, records.filter { it.id != item.id })
            return json(request, 204, null)
        }
        return error(request, 405, "METHOD_NOT_ALLOWED")
    }
    private fun details(id: String, records: List<DemoStoredReview>): BookDetailsDto {
        val book = requireNotNull(library.book(id))
        val imagePrefix = "android.resource://ru.mareanexx.elementa/"
        val images = if (id == "book_12") emptyList() else listOf(BookImageDto("cover", imagePrefix + if (id == "book_1") R.drawable.book_example else R.drawable.book_demo_cover, "Обложка")) +
            if (id == "book_1") listOf(BookImageDto("spread", imagePrefix + R.drawable.book_demo_spread, "Разворот · демо"), BookImageDto("diagram", imagePrefix + R.drawable.book_demo_diagram, "Иллюстрация · демо")) else emptyList()
        val description = book.description + "\n\nВ книге последовательно разобраны строение молекул, химические связи и закономерности протекания реакций. Теория сопровождается схемами, примерами и вопросами для самостоятельной проверки.\n\nОтдельное внимание уделено тому, как читать структурные формулы, сравнивать свойства соединений и объяснять механизмы превращений. Материал удобно изучать небольшими разделами, возвращаясь к ключевым определениям и делая собственные заметки.\n\nИздание подойдёт для повторения курса и подготовки к практическим занятиям. Перед сложными темами полезно освежить базовые понятия общей химии."
        return BookDetailsDto(book.id, book.title, book.authors, description, images, book.tags.map { BookTagDto(it.id, it.label, it.kind.name.lowercase()) },
            book.pages, book.pagesRead, 5_600_000L + book.id.removePrefix("book_").toLong() * 100_000, 12, rating(records), shelves(id),
            BookMetadataDto("Учебная редакция Elementa", 2022, "Русский", null, "EPUB"), records.firstOrNull { it.authorId == userId }?.let(::review),
            records.sortedByDescending { it.baseLikes }.take(2).map(::review))
    }
    private fun shelves(id: String): BookShelvesDto {
        val all = library.collections()
        return BookShelvesDto(all.any { it.isFavorite && library.contains(it.id, id) }, all.any { it.isReadingList && library.contains(it.id, id) },
            all.count { !it.isSystem && library.contains(it.id, id) })
    }
    private fun rating(items: List<DemoStoredReview>) = BookRatingDto(if (items.isEmpty()) null else items.map { it.rating }.average(), items.size, (1..5).map { stars -> items.count { it.rating == stars } })
    private fun cacheRating(id: String, items: List<DemoStoredReview>) { val result = rating(items); library.cacheRating(id, result.average, items.size) }
    private fun review(item: DemoStoredReview) = BookReviewDto(item.id, item.authorId, item.authorName, item.avatarUrl, item.rating, item.text, item.createdAt, item.updatedAt,
        item.baseLikes + item.reactions.values.count { it == "like" }, item.baseDislikes + item.reactions.values.count { it == "dislike" }, item.reactions[userId], store.replies(item.id).size, item.authorId == userId)
    private fun reply(item: DemoStoredReply) = BookReplyDto(item.id, item.authorName, item.avatarUrl, item.text, item.createdAt, item.authorId == userId)
    private fun author(): Pair<String, String?> {
        val saved = context.getSharedPreferences("profile_demo_server", Context.MODE_PRIVATE).getString("profile_$userId", null)
        val profile = saved?.let { runCatching { JsonParser.parseString(it).asJsonObject }.getOrNull() }
        return (profile?.get("full_name")?.takeUnless { it.isJsonNull }?.asString ?: fullName) to profile?.get("avatar_url")?.takeUnless { it.isJsonNull }?.asString
    }
    private fun offset(request: Request, ids: List<String>): Int? {
        val raw = request.url.queryParameter("cursor") ?: return 0
        return ids.indexOf(raw).takeIf { it >= 0 }?.plus(1)
    }
    private fun body(request: Request) = Buffer().also { request.body?.writeTo(it) }.readUtf8()
    private fun error(request: Request, status: Int, code: String) = json(request, status, mapOf("error" to mapOf("code" to code)))
    private fun json(request: Request, status: Int, value: Any?) = Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(status)
        .message("Демонстрационный ответ").body((if (status == 204) "" else gson.toJson(value)).toResponseBody("application/json".toMediaType())).build()
}
