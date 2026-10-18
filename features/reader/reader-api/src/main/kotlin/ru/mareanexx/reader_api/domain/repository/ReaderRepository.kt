package ru.mareanexx.reader_api.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mareanexx.book_api.domain.model.BookShelves
import ru.mareanexx.reader_api.domain.model.*

interface ReaderRepository {
    val transfers: Flow<List<BookTransfer>>
    val changes: Flow<Unit>
    val settings: Flow<ReaderSettings>
    fun annotations(bookId: String): Flow<List<ReaderAnnotation>>
    fun syncState(bookId: String): Flow<ReaderSyncState>
    suspend fun requestDownload(bookId: String, pinned: Boolean = true): ReaderResult<Unit>
    suspend fun cancelDownload(bookId: String): ReaderResult<Unit>
    suspend fun removeDownload(bookId: String): ReaderResult<Unit>
    suspend fun updateLibraryState(bookId: String, shelves: BookShelves, pagesRead: Int? = null): ReaderResult<Unit>
    suspend fun downloadedBooks(): ReaderResult<List<OfflineBook>>
    suspend fun openBook(bookId: String): ReaderResult<ReaderDocument>
    fun retainBook(bookId: String, leaseId: String)
    fun releaseBook(bookId: String, leaseId: String = "legacy")
    suspend fun saveSettings(value: ReaderSettings): ReaderResult<Unit>
    suspend fun savePosition(value: ReaderPosition): ReaderResult<Unit>
    suspend fun saveAnnotation(value: ReaderAnnotation): ReaderResult<Unit>
    suspend fun deleteAnnotation(id: String): ReaderResult<Unit>
    suspend fun synchronize(bookId: String)
}
