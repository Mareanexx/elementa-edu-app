package ru.mareanexx.collections_impl

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import ru.mareanexx.collections_api.domain.model.*
import ru.mareanexx.collections_api.domain.repository.CollectionsRepository

internal class FakeCollectionsRepository : CollectionsRepository {
    override val preferences = MutableStateFlow(CollectionsPreferences())
    var items = listOf(BookCollection("fav", "Избранное", CollectionCover.Chemistry, 0, true))
    var books = emptyList<CollectionBook>()
    var failure: CollectionsError? = null
    var gate: CompletableDeferred<Unit>? = null
    var libraryCalls = 0
    var libraryGate: CompletableDeferred<Unit>? = null
    var booksGate: CompletableDeferred<Unit>? = null
    var saves = 0
    var deletes = 0
    var added = emptySet<String>()
    var lastForm: CollectionForm? = null
    private fun <T> result(value: T): CollectionsResult<T> = failure?.let { CollectionsResult.Failure(it) } ?: CollectionsResult.Success(value)
    override suspend fun getCollections() = result(items)
    override suspend fun uploadCover(upload: CollectionCoverUpload) = result(CollectionImage("cover", "https://example.test/cover.jpg"))
    override suspend fun create(form: CollectionForm): CollectionsResult<BookCollection> {
        saves++
        lastForm = form
        gate?.await()
        return result(BookCollection("new", form.name, form.cover, 0))
    }
    override suspend fun update(id: String, form: CollectionForm): CollectionsResult<BookCollection> {
        saves++
        lastForm = form
        return result(BookCollection(id, form.name, form.cover, 0))
    }
    override suspend fun delete(id: String): CollectionsResult<Unit> { deletes++; return result(Unit) }
    override suspend fun setPreferences(value: CollectionsPreferences): CollectionsResult<Unit> {
        if (failure == null) preferences.value = value
        return result(Unit)
    }
    override suspend fun getBooks(id: String): CollectionsResult<List<CollectionBook>> {
        val snapshot = result(books)
        booksGate?.await()
        return snapshot
    }
    override suspend fun getLibrary(): CollectionsResult<List<CollectionBook>> { libraryCalls++; libraryGate?.await(); return result(books) }
    override suspend fun addBooks(id: String, bookIds: Set<String>): CollectionsResult<Unit> { added = bookIds; return result(Unit) }
    override suspend fun removeBook(id: String, bookId: String) = result(Unit)
}
