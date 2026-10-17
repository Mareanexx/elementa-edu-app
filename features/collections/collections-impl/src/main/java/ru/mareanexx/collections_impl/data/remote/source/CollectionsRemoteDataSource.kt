package ru.mareanexx.collections_impl.data.remote.source

import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import ru.mareanexx.collections_api.domain.model.*
import ru.mareanexx.collections_impl.data.remote.api.CollectionsApi
import ru.mareanexx.collections_impl.data.remote.dto.*
import ru.mareanexx.collections_impl.data.remote.mapper.*
import java.io.EOFException
import java.io.IOException
import java.net.SocketTimeoutException

internal class CollectionsRemoteDataSource(
    private val api: CollectionsApi,
) {
    suspend fun uploadCover(upload: CollectionCoverUpload): CollectionsResult<CollectionImage> {
        if (upload.bytes.isEmpty() || upload.bytes.size > 2 * 1024 * 1024) return CollectionsResult.Failure(CollectionsError.InvalidImage)
        return request({ api.uploadCover(MultipartBody.Part.createFormData("image", "collection-cover.jpg", upload.bytes.toRequestBody("image/jpeg".toMediaType()))) }, CollectionMapper::image)
    }
    suspend fun getPreferences() = request({ api.getPreferences() }, CollectionMapper::preferences)
    suspend fun setPreferences(value: CollectionsPreferences) = request({ api.setPreferences(CollectionMapper.preferences(value)) }, CollectionMapper::preferences)
    suspend fun getCollections() = request({ api.getCollections() }) {
        requireNotNull(it.items).map(CollectionMapper::collection).also { items ->
            require(items.map { it.id }.distinct().size == items.size)
            require(items.count { it.isFavorite } == 1)
        }
    }
    suspend fun create(form: CollectionForm) = request({ api.create(SaveCollectionRequest(form.name, CollectionMapper.key(form.cover), form.image?.id)) }, CollectionMapper::collection)
    suspend fun update(id: String, form: CollectionForm) = request({ api.update(id, SaveCollectionRequest(form.name, CollectionMapper.key(form.cover), form.image?.id)) }, CollectionMapper::collection)
    suspend fun getBooks(id: String) = request({ api.getBooks(id) }) { mapBooks(it) }
    suspend fun getLibrary() = request({ api.getLibrary() }) { mapBooks(it) }
    suspend fun delete(id: String) = action { api.delete(id) }
    suspend fun addBooks(id: String, ids: Set<String>) = action { api.addBooks(id, CollectionBooksRequest(ids.sorted())) }
    suspend fun removeBook(id: String, bookId: String) = action { api.removeBook(id, bookId) }
    private fun mapBooks(value: CollectionBooksResponse) = requireNotNull(value.items).map(CollectionMapper::book).also {
        require(it.map { book -> book.id }.distinct().size == it.size)
    }
    private suspend fun <T, R> request(call: suspend () -> Response<T>, map: (T) -> R): CollectionsResult<R> = execute {
        val response = call()
        if (response.isSuccessful) CollectionsResult.Success(map(requireNotNull(response.body())))
        else CollectionsResult.Failure(CollectionsErrorMapper.map(response))
    }
    private suspend fun action(call: suspend () -> Response<Unit>): CollectionsResult<Unit> = execute {
        val response = call()
        if (response.isSuccessful) CollectionsResult.Success(Unit) else CollectionsResult.Failure(CollectionsErrorMapper.map(response))
    }
    private suspend fun <T> execute(block: suspend () -> CollectionsResult<T>): CollectionsResult<T> = withContext(Dispatchers.IO) {
        try { block()
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: SocketTimeoutException) { CollectionsResult.Failure(CollectionsError.Timeout)
        } catch (_: MalformedJsonException) { CollectionsResult.Failure(CollectionsError.InvalidResponse)
        } catch (_: EOFException) { CollectionsResult.Failure(CollectionsError.InvalidResponse)
        } catch (_: IOException) { CollectionsResult.Failure(CollectionsError.Offline)
        } catch (_: RuntimeException) { CollectionsResult.Failure(CollectionsError.InvalidResponse) }
    }
}
