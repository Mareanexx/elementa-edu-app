package ru.mareanexx.user_library_impl.data.local

import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_api.domain.repository.ReaderRepository
import ru.mareanexx.user_library_api.domain.model.*
import java.security.MessageDigest
import java.util.Locale

/** Список скачанного работает без сети; курсор привязан к запросу и снимку локальных файлов. */
internal class DownloadedLibrarySource(
    private val reader: ReaderRepository,
) {
    suspend fun update(book: LibraryBook) { reader.updateLibraryState(book.id, book.shelves, book.pagesRead) }
    suspend fun count() = (reader.downloadedBooks() as? ReaderResult.Success)?.value?.size ?: 0
    suspend fun books(query: LibraryQuery, cursor: String?, lastCounts: LibraryCounts?): LibraryResult<LibraryPage> {
        val result = reader.downloadedBooks()
        if (result is ReaderResult.Failure) return LibraryResult.Failure(LibraryError.Storage)
        val all = (result as ReaderResult.Success).value
        val search = query.search.trim().lowercase(Locale.ROOT)
        val filtered = all.filter { value -> search.isEmpty() || listOf(value.book.title, value.book.authors, value.book.tags.joinToString(" ") { it.label }).any { it.lowercase(Locale.ROOT).contains(search) } }
        val ordered = when (query.sort) {
            LibrarySort.Recent -> filtered.sortedWith(compareByDescending<OfflineBook> { it.downloadedAt }.thenBy { it.book.id })
            LibrarySort.Title -> filtered.sortedWith(compareBy<OfflineBook> { it.book.title.lowercase(Locale.ROOT) }.thenBy { it.book.id })
            LibrarySort.Author -> filtered.sortedWith(compareBy<OfflineBook> { it.book.authors.lowercase(Locale.ROOT) }.thenBy { it.book.id })
        }
        val signature = MessageDigest.getInstance("SHA-256").digest((query.toString() + ordered.joinToString { "${it.book.id}:${it.downloadedAt}" }).toByteArray()).joinToString("") { "%02x".format(it) }.take(20)
        val offset = if (cursor == null) 0 else {
            val parts = cursor.split(':')
            if (parts.size != 2 || parts[0] != signature) return LibraryResult.Failure(LibraryError.Conflict)
            parts[1].toIntOrNull()?.takeIf { it in 0..ordered.size } ?: return LibraryResult.Failure(LibraryError.Conflict)
        }
        val items = ordered.drop(offset).take(20).map { value ->
            val book = value.book
            LibraryBook(book.id, book.title, book.authors, book.pages, value.pagesRead,
                when (value.pagesRead) { 0 -> ReadingStatus.NotStarted; book.pages -> ReadingStatus.Completed; else -> ReadingStatus.InProgress },
                book.coverUrl, book.coverKey, book.ratingAverage?.let { LibraryRating(it, book.ratingCount) }, book.tags, book.shelves)
        }
        val counts = (lastCounts ?: LibraryCounts(all.size, all.count { it.pagesRead in 1 until it.book.pages }, all.count { it.pagesRead == it.book.pages }, all.count { it.book.shelves.wantToRead })).copy(downloaded = all.size)
        val next = offset + items.size
        return LibraryResult.Success(LibraryPage(items, if (next < ordered.size) "$signature:$next" else null, ordered.size, counts))
    }
}
