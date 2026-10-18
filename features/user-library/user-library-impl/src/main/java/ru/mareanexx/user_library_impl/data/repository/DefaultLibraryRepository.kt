package ru.mareanexx.user_library_impl.data.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_api.domain.repository.LibraryRepository
import ru.mareanexx.user_library_impl.data.local.LibrarySearchHistory
import ru.mareanexx.user_library_impl.data.remote.source.LibraryRemoteDataSource

internal class DefaultLibraryRepository(
    private val remote: LibraryRemoteDataSource,
    private val history: LibrarySearchHistory,
) : LibraryRepository {
    private val mutableChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    override val changes = mutableChanges.asSharedFlow()
    override val recentQueries = history.queries
    override suspend fun getBooks(query: LibraryQuery, cursor: String?) = remote.books(query, cursor)
    override suspend fun getPreferences() = remote.preferences()
    override suspend fun savePreferences(value: LibraryPreferences) = remote.savePreferences(value)
    override suspend fun setReadingStatus(bookId: String, status: ReadingStatus) = remote.status(bookId, status).also {
        if (it is LibraryResult.Success) mutableChanges.emit(Unit)
    }
    override suspend fun rememberQuery(query: String) = history.remember(query)
    override suspend fun clearRecentQueries() = history.clear()
}
