package ru.mareanexx.user_library_impl.data.remote.mapper

import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_impl.data.remote.dto.*

internal object LibraryMapper {
    fun book(dto: LibraryBookDto): LibraryBook {
        val pages = requireNotNull(dto.pages).also { require(it > 0) }
        val read = (dto.pagesRead ?: 0).coerceIn(0, pages)
        val status = when (dto.status) {
            "not_started" -> ReadingStatus.NotStarted
            "in_progress" -> ReadingStatus.InProgress
            "completed" -> ReadingStatus.Completed
            else -> error("Неизвестный статус чтения")
        }
        require(when (status) {
            ReadingStatus.NotStarted -> read == 0
            ReadingStatus.InProgress -> read < pages
            ReadingStatus.Completed -> read == pages
        })
        val rating = dto.rating?.let { value ->
            val average = value.average
            val count = value.count
            if (average != null && average.isFinite() && average in 1.0..5.0 && count != null && count > 0) LibraryRating(average, count) else null
        }
        val tags = dto.tags.orEmpty().mapNotNull { tag ->
            val id = tag.id?.takeIf(String::isNotBlank)
            val label = tag.label?.takeIf(String::isNotBlank)
            if (id == null || label == null) null else BookTag(id, label, BookTagKind.entries.firstOrNull { it.name.equals(tag.kind, true) } ?: BookTagKind.Topic)
        }.distinctBy { it.id }
        return LibraryBook(
            required(dto.id), required(dto.title), required(dto.authors), pages, read, status,
            dto.coverUrl?.takeIf(String::isNotBlank), dto.coverKey.orEmpty(), rating, tags,
            BookShelves(requireNotNull(dto.favorite), requireNotNull(dto.wantToRead), nonnegative(dto.collectionCount).also { require(it <= 30) }),
        )
    }
    fun page(dto: LibraryPageDto): LibraryPage {
        val counts = requireNotNull(dto.counts).let { LibraryCounts(nonnegative(it.all), nonnegative(it.inProgress), nonnegative(it.completed), nonnegative(it.wantToRead)) }
        require(counts.inProgress.toLong() + counts.completed <= counts.all && counts.wantToRead <= counts.all)
        val items = requireNotNull(dto.items).map(::book).distinctBy { it.id }
        val total = nonnegative(dto.totalCount).also { require(it <= counts.all && it >= items.size) }
        val highlights = dto.highlights?.let { LibraryHighlights(it.inProgress.orEmpty().map(::book), it.completed.orEmpty().map(::book), it.wantToRead.orEmpty().map(::book)) }
        return LibraryPage(items, dto.nextCursor?.takeIf(String::isNotBlank), total, counts, highlights)
    }
    fun preferences(dto: LibraryPreferencesDto) = LibraryPreferences(when (dto.sort) {
        "recent" -> LibrarySort.Recent
        "title_asc" -> LibrarySort.Title
        "author_asc" -> LibrarySort.Author
        else -> error("Неизвестная сортировка")
    })
    fun sort(sort: LibrarySort): String = when (sort) {
        LibrarySort.Recent -> "recent"
        LibrarySort.Title -> "title_asc"
        LibrarySort.Author -> "author_asc"
    }
    fun category(category: LibraryCategory): String = when (category) {
        LibraryCategory.Downloaded -> error("Скачанные книги читаются из локального хранилища")
        LibraryCategory.All -> "all"
        LibraryCategory.InProgress -> "in_progress"
        LibraryCategory.Completed -> "completed"
        LibraryCategory.WantToRead -> "want_to_read"
    }
    fun status(status: ReadingStatus): String = when (status) {
        ReadingStatus.NotStarted -> "not_started"
        ReadingStatus.InProgress -> "in_progress"
        ReadingStatus.Completed -> "completed"
    }
    private fun required(value: String?) = requireNotNull(value).also { require(it.isNotBlank()) }
    private fun nonnegative(value: Int?) = requireNotNull(value).also { require(it >= 0) }
}
