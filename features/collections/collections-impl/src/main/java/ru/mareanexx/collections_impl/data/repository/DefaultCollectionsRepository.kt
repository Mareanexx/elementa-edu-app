package ru.mareanexx.collections_impl.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.mareanexx.collections_api.domain.model.*
import ru.mareanexx.collections_api.domain.repository.CollectionsRepository
import ru.mareanexx.collections_impl.data.local.CollectionsPreferencesSource
import ru.mareanexx.collections_impl.data.remote.source.CollectionsRemoteDataSource

internal class DefaultCollectionsRepository(
    private val remote: CollectionsRemoteDataSource,
    private val local: CollectionsPreferencesSource,
) : CollectionsRepository {
    private val preferencesMutex = Mutex()
    override val preferences = local.flow
    override suspend fun setPreferences(value: CollectionsPreferences): CollectionsResult<Unit> = preferencesMutex.withLock {
        when (val result = remote.setPreferences(value)) {
            is CollectionsResult.Failure -> result
            is CollectionsResult.Success -> local.save(result.value)
        }
    }
    override suspend fun getCollections(): CollectionsResult<List<BookCollection>> {
        val settings = preferencesMutex.withLock {
            when (val result = remote.getPreferences()) {
                is CollectionsResult.Failure -> result
                is CollectionsResult.Success -> local.save(result.value)
            }
        }
        if (settings is CollectionsResult.Failure) return settings
        return remote.getCollections()
    }
    override suspend fun uploadCover(upload: CollectionCoverUpload) = remote.uploadCover(upload)
    override suspend fun create(form: CollectionForm) = remote.create(form)
    override suspend fun update(id: String, form: CollectionForm) = remote.update(id, form)
    override suspend fun delete(id: String) = remote.delete(id)
    override suspend fun getBooks(id: String) = remote.getBooks(id)
    override suspend fun getLibrary() = remote.getLibrary()
    override suspend fun addBooks(id: String, bookIds: Set<String>) = remote.addBooks(id, bookIds)
    override suspend fun removeBook(id: String, bookId: String) = remote.removeBook(id, bookId)
}
