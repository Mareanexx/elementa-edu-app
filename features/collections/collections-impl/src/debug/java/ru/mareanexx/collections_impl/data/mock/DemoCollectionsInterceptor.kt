package ru.mareanexx.collections_impl.data.mock

import android.content.Context
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import ru.mareanexx.collections_api.domain.model.CollectionsError
import ru.mareanexx.collections_api.domain.validation.CollectionRules
import ru.mareanexx.collections_impl.data.remote.dto.*
import java.io.IOException
import java.util.UUID

internal class DemoCollectionsInterceptor(
    context: Context,
    userId: String,
) : Interceptor {
    private val covers = DemoCollectionCoverStore(context, userId)
    private val store = DemoCollectionsStore(context, userId)
    private val controls = context.getSharedPreferences("collections_mock_controls", Context.MODE_PRIVATE)
    private val gson = Gson()
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val base = "/api/v1/users/me/collections"
        if (path != "/api/v1/users/me/collection-covers" && path != "/api/v1/users/me/collection-preferences" && path != "/api/v1/users/me/library/books" && path != base && !path.startsWith("$base/")) return chain.proceed(request)
        Thread.sleep(350)
        if (!request.header("Authorization").orEmpty().startsWith("Bearer ")) return error(request, 401, "UNAUTHORIZED")
        val forced = synchronized(DemoCollectionsStore.lock) {
            val value = controls.getInt("next_status", 0)
            if (value != 0) controls.edit().remove("next_status").commit()
            value
        }
        if (forced == -1) throw IOException("Демонстрационный сбой подключения")
        if (forced > 0) return error(request, forced, "MOCK_ERROR")
        return synchronized(DemoCollectionsStore.lock) {
            try { handle(request, base, path) }
            catch (error: IOException) { throw error }
            catch (_: RuntimeException) { error(request, 500, "MOCK_STORAGE_ERROR") }
        }
    }
    private fun handle(request: Request, base: String, path: String): Response {
        if (path == "/api/v1/users/me/collection-covers") {
            if (request.method != "POST") return error(request, 405, "METHOD_NOT_ALLOWED")
            val image = (request.body as? MultipartBody)?.parts?.singleOrNull()?.body ?: return error(request, 422, "INVALID_COVER_IMAGE")
            if (image.contentType()?.toString() != "image/jpeg" || image.contentLength() > 2 * 1024 * 1024) return error(request, 413, "INVALID_COVER_IMAGE")
            val bytes = Buffer().also { image.writeTo(it) }.readByteArray()
            return try { json(request, 201, covers.save(bytes)) }
            catch (_: IllegalArgumentException) { error(request, 422, "INVALID_COVER_IMAGE") }
        }
        if (path == "/api/v1/users/me/collection-preferences") {
            if (request.method == "GET") return json(request, 200, store.preferences())
            if (request.method == "PATCH") {
                val value = runCatching { gson.fromJson(body(request), CollectionPreferencesDto::class.java) }.getOrNull()
                    ?: return error(request, 422, "INVALID_PREFERENCES")
                if (value.sortOrder !in listOf("name_asc", "name_desc", "books_asc", "books_desc") || value.showEmpty == null) return error(request, 422, "INVALID_PREFERENCES")
                store.savePreferences(value)
                return json(request, 200, value)
            }
            return error(request, 405, "METHOD_NOT_ALLOWED")
        }
        if (path == "/api/v1/users/me/library/books") return json(request, 200, CollectionBooksResponse(DemoBookCatalog.items))
        val records = store.read()
        val parts = path.removePrefix(base).trim('/').split('/').filter(String::isNotEmpty)
        val current = parts.firstOrNull()?.let { id -> records.firstOrNull { it.id == id } }
        if (parts.isNotEmpty() && current == null) return error(request, 404, "COLLECTION_NOT_FOUND")
        if (parts.isEmpty() && request.method == "GET") return json(request, 200, CollectionsResponse(records.map(::dto)))
        if (parts.isEmpty() && request.method == "POST" || parts.size == 1 && request.method == "PATCH") {
            if (current?.favorite == true) return error(request, 403, "COLLECTION_PROTECTED")
            val form = runCatching { gson.fromJson(body(request), SaveCollectionRequest::class.java) }.getOrNull() ?: return error(request, 422, "NAME_REQUIRED")
            val name = CollectionRules.normalize(form.name.orEmpty())
            CollectionRules.validate(name)?.let { return error(request, 422, if (it == CollectionsError.NameRequired) "NAME_REQUIRED" else "NAME_TOO_LONG") }
            if (records.any { it.id != current?.id && it.name.equals(name, ignoreCase = true) }) return error(request, 409, "NAME_TAKEN")
            if (current == null && records.count { !it.favorite } >= CollectionRules.MAX_COLLECTIONS) return error(request, 409, "COLLECTION_LIMIT_REACHED")
            if (form.coverKey !in listOf("chemistry", "molecules", "reactions", "laboratory", "notes", "space")) return error(request, 422, "INVALID_COVER")
            if (form.imageId != null && covers.get(form.imageId) == null) return error(request, 422, "INVALID_COVER_IMAGE")
            val item = DemoStoredCollection(current?.id ?: UUID.randomUUID().toString(), name, requireNotNull(form.coverKey), false, current?.bookIds ?: emptyList(), form.imageId)
            store.save(if (current == null) records + item else records.map { if (it.id == item.id) item else it })
            return json(request, if (current == null) 201 else 200, dto(item))
        }
        if (parts.size == 1 && request.method == "DELETE") {
            if (current!!.favorite) return error(request, 403, "COLLECTION_PROTECTED")
            store.save(records.filter { it.id != current.id })
            return json(request, 204, null)
        }
        if (parts.getOrNull(1) == "books") {
            val collection = requireNotNull(current)
            if (parts.size == 2 && request.method == "GET") return json(request, 200, CollectionBooksResponse(collection.bookIds.map { id -> DemoBookCatalog.items.first { it.id == id } }))
            if (parts.size == 2 && request.method == "POST") {
                val ids = runCatching { gson.fromJson(body(request), CollectionBooksRequest::class.java).bookIds }.getOrNull()
                    ?: return error(request, 422, "INVALID_BOOKS")
                if (ids.isEmpty() || ids.any { id -> DemoBookCatalog.items.none { it.id == id } }) return error(request, 422, "INVALID_BOOKS")
                val updated = collection.copy(bookIds = (collection.bookIds + ids).distinct())
                store.save(records.map { if (it.id == updated.id) updated else it })
                return json(request, 204, null)
            }
            if (parts.size == 3 && request.method == "DELETE") {
                store.save(records.map { if (it.id == collection.id) it.copy(bookIds = it.bookIds - parts[2]) else it })
                return json(request, 204, null)
            }
        }
        return error(request, 405, "METHOD_NOT_ALLOWED")
    }
    private fun dto(item: DemoStoredCollection) = CollectionDto(item.id, item.name, item.coverKey, item.bookIds.size, if (item.favorite) "favorite" else "custom", covers.get(item.coverImageId))
    private fun body(request: Request) = Buffer().also { request.body?.writeTo(it) }.readUtf8()
    private fun error(request: Request, status: Int, code: String) = json(request, status, mapOf("error" to mapOf("code" to code)))
    private fun json(request: Request, status: Int, value: Any?) = Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
        .code(status).message("Демонстрационный ответ").body((if (status == 204) "" else gson.toJson(value)).toResponseBody("application/json".toMediaType())).build()
}
