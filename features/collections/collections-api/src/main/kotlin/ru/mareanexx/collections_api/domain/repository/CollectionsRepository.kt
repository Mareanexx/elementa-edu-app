package ru.mareanexx.collections_api.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mareanexx.collections_api.domain.model.*

interface CollectionsRepository {
    val preferences: Flow<CollectionsPreferences>
    suspend fun setPreferences(value: CollectionsPreferences): CollectionsResult<Unit>
    suspend fun getCollections(): CollectionsResult<List<BookCollection>>
    suspend fun uploadCover(upload: CollectionCoverUpload): CollectionsResult<CollectionImage>
    suspend fun create(form: CollectionForm): CollectionsResult<BookCollection>
    suspend fun update(id: String, form: CollectionForm): CollectionsResult<BookCollection>
    suspend fun delete(id: String): CollectionsResult<Unit>
    suspend fun getBooks(id: String): CollectionsResult<List<CollectionBook>>
    suspend fun getLibrary(): CollectionsResult<List<CollectionBook>>
    suspend fun addBooks(id: String, bookIds: Set<String>): CollectionsResult<Unit>
    suspend fun removeBook(id: String, bookId: String): CollectionsResult<Unit>
}
