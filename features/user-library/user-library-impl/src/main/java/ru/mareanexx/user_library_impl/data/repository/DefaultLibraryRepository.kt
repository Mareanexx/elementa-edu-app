package ru.mareanexx.user_library_impl.data.repository

import ru.mareanexx.user_library_impl.data.local.DownloadedLibrarySource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_api.domain.repository.LibraryRepository
import ru.mareanexx.user_library_impl.data.local.LibrarySearchHistory
import ru.mareanexx.user_library_impl.data.remote.source.LibraryRemoteDataSource

internal class DefaultLibraryRepository(
    private val remote: LibraryRemoteDataSource,
    private val history: LibrarySearchHistory,
    private val offline: DownloadedLibrarySource,
) : LibraryRepository {
    private val mutableChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    override val changes = mutableChanges.asSharedFlow()
    override val recentQueries = history.queries
    private var counts: LibraryCounts? = null
    override suspend fun getBooks(query: LibraryQuery, cursor: String?): LibraryResult<LibraryPage> {
        if (query.category == LibraryCategory.Downloaded) return offline.books(query, cursor, counts)
        val result = remote.books(query, cursor)
        return if (result is LibraryResult.Success) {
            val next = result.value.counts.copy(downloaded = offline.count())
            counts = next
            LibraryResult.Success(result.value.copy(counts = next))
        } else result
    }
    override suspend fun getPreferences() = remote.preferences()
    override suspend fun savePreferences(value: LibraryPreferences) = remote.savePreferences(value)
    override suspend fun setReadingStatus(bookId: String, status: ReadingStatus) = remote.status(bookId, status).also {
        if (it is LibraryResult.Success) { offline.update(it.value); mutableChanges.emit(Unit) }
    }
    override suspend fun rememberQuery(query: String) = history.remember(query)
    override suspend fun clearRecentQueries() = history.clear()
}
