package ru.mareanexx.reader_impl

import kotlinx.coroutines.flow.*
import ru.mareanexx.book_api.domain.model.BookShelves
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_api.domain.repository.ReaderRepository

internal class FakeReaderRepository : ReaderRepository {
    override val transfers = MutableStateFlow(emptyList<BookTransfer>())
    override val changes = MutableSharedFlow<Unit>()
    override val settings = MutableStateFlow(ReaderSettings())
    private val notes = MutableStateFlow(emptyList<ReaderAnnotation>())
    val positions = mutableListOf<ReaderPosition>()
    val savedSettings = mutableListOf<ReaderSettings>()
    var failSave: ReaderError? = null
    var openFailure: ReaderError? = null
    var positionFailure: ReaderError? = null
    val book = ReaderBook("book", "Химия", "Автор", 160, 0, null, "chemistry", null, 0, emptyList(), BookShelves(false, false, 0))
    var document = ReaderDocument(book, "v1", "/tmp/reader", listOf(ReaderChapter("chapter_1", "Атомы")), null)
    override fun annotations(bookId: String): Flow<List<ReaderAnnotation>> = notes
    override fun syncState(bookId: String): Flow<ReaderSyncState> = flowOf(ReaderSyncState())
    override suspend fun requestDownload(bookId: String, pinned: Boolean) = ReaderResult.Success(Unit)
    override suspend fun cancelDownload(bookId: String) = ReaderResult.Success(Unit)
    override suspend fun removeDownload(bookId: String) = ReaderResult.Success(Unit)
    override suspend fun updateLibraryState(bookId: String, shelves: BookShelves, pagesRead: Int?) = ReaderResult.Success(Unit)
    override suspend fun downloadedBooks(): ReaderResult<List<OfflineBook>> = ReaderResult.Success(emptyList())
    override suspend fun openBook(bookId: String): ReaderResult<ReaderDocument> = openFailure?.let { ReaderResult.Failure(it) } ?: ReaderResult.Success(document)
    override fun retainBook(bookId: String, leaseId: String) = Unit
    override fun releaseBook(bookId: String, leaseId: String) = Unit
    override suspend fun saveSettings(value: ReaderSettings): ReaderResult<Unit> { savedSettings += value; settings.value = value; return ReaderResult.Success(Unit) }
    override suspend fun savePosition(value: ReaderPosition): ReaderResult<Unit> { positionFailure?.let { return ReaderResult.Failure(it) }; positions += value; return ReaderResult.Success(Unit) }
    override suspend fun saveAnnotation(value: ReaderAnnotation): ReaderResult<Unit> {
        failSave?.let { return ReaderResult.Failure(it) }
        notes.value = notes.value.filterNot { it.id == value.id } + value
        return ReaderResult.Success(Unit)
    }
    override suspend fun deleteAnnotation(id: String): ReaderResult<Unit> { notes.value = notes.value.filterNot { it.id == id }; return ReaderResult.Success(Unit) }
    override suspend fun synchronize(bookId: String) = Unit
}
