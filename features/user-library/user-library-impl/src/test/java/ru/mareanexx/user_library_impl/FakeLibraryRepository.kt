package ru.mareanexx.user_library_impl

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_api.domain.repository.LibraryRepository

internal class FakeLibraryRepository : LibraryRepository {
    override val changes = MutableSharedFlow<Unit>()
    override val recentQueries = MutableStateFlow(emptyList<String>())
    val calls = mutableListOf<Pair<LibraryQuery, String?>>()
    val gates = mutableMapOf<String, CompletableDeferred<Unit>>()
    val pages = mutableMapOf<String?, LibraryPage>(null to LibraryFixtures.page())
    var failure: LibraryError? = null
    var sortFailure: LibraryError? = null
    var statusFailure: LibraryError? = null
    var preferences = LibraryPreferences()
    var savedPreferences = mutableListOf<LibraryPreferences>()
    override suspend fun getBooks(query: LibraryQuery, cursor: String?): LibraryResult<LibraryPage> {
        calls += query to cursor
        val result = failure?.let { LibraryResult.Failure(it) } ?: LibraryResult.Success(pages.getValue(cursor))
        gates[query.search]?.await()
        return result
    }
    override suspend fun getPreferences() = LibraryResult.Success(preferences)
    override suspend fun savePreferences(value: LibraryPreferences): LibraryResult<LibraryPreferences> {
        savedPreferences += value
        if (sortFailure != null) return LibraryResult.Failure(sortFailure!!)
        preferences = value
        return LibraryResult.Success(value)
    }
    override suspend fun setReadingStatus(bookId: String, status: ReadingStatus): LibraryResult<LibraryBook> {
        statusFailure?.let { return LibraryResult.Failure(it) }
        val book = LibraryFixtures.book(bookId).copy(status = status, pagesRead = if (status == ReadingStatus.Completed) 160 else 0)
        pages[null] = LibraryFixtures.page(listOf(book))
        return LibraryResult.Success(book)
    }
    override suspend fun rememberQuery(query: String): LibraryResult<Unit> {
        recentQueries.value = (listOf(query) + recentQueries.value.filterNot { it.equals(query, true) }).take(8)
        return LibraryResult.Success(Unit)
    }
    override suspend fun clearRecentQueries(): LibraryResult<Unit> { recentQueries.value = emptyList(); return LibraryResult.Success(Unit) }
}
