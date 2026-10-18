package ru.mareanexx.catalog_impl.data.remote.mapper

import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_impl.data.remote.dto.*
import java.time.Instant

internal object CatalogMapper {
    fun book(dto: CatalogBookDto): CatalogBook {
        val pages = positive(dto.pages)
        val read = nonnegative(dto.pagesRead).also { require(it <= pages) }
        val rating = dto.rating?.let {
            val average = requireNotNull(it.average).also { value -> require(value.isFinite() && value in 1.0..5.0) }
            CatalogRating(average, positive(it.count))
        }
        return CatalogBook(text(dto.id), text(dto.title), text(dto.authors), pages, read, dto.coverUrl?.takeIf(String::isNotBlank), dto.coverKey.orEmpty(), rating,
            dto.tags.orEmpty().map { BookTag(text(it.id), text(it.label), BookTagKind.entries.firstOrNull { kind -> kind.name.equals(it.kind, true) } ?: BookTagKind.Topic) }.distinctBy { it.id },
            BookShelves(requireNotNull(dto.favorite), requireNotNull(dto.wantToRead), nonnegative(dto.collectionCount).also { require(it <= 30) }))
    }
    fun category(dto: CategoryDto) = CatalogCategory(text(dto.id), text(dto.title), text(dto.description), dto.iconKey.orEmpty(), nonnegative(dto.bookCount))
    fun page(dto: CatalogPageDto): CatalogPage {
        val items = requireNotNull(dto.items).map(::book).distinctBy { it.id }
        return CatalogPage(items, dto.nextCursor?.takeIf(String::isNotBlank), nonnegative(dto.total).also { require(it >= items.size) })
    }
    fun unread(dto: UnreadDto) = UnreadState(nonnegative(dto.count), requireNotNull(dto.revision).also { require(it >= 0) })
    fun home(dto: HomeFeedDto) = HomeFeed(requireNotNull(dto.continueReading).map(::book).also { books -> require(books.all { it.pagesRead in 1 until it.pages }) }.distinctBy { it.id }.take(5),
        requireNotNull(dto.categories).map(::category), requireNotNull(dto.sections).map { HomeSection(text(it.id), requireNotNull(it.books).map(::book), sort(text(it.sort))) },
        nonnegative(dto.libraryCount), nonnegative(dto.collectionCount), unread(requireNotNull(dto.unread)))
    fun notification(dto: NotificationDto): CatalogNotification {
        require(dto.bookId.isNullOrBlank() || dto.categoryId.isNullOrBlank())
        return CatalogNotification(text(dto.id), requireNotNull(dto.sequence).also { require(it > 0) }, text(dto.title), text(dto.body), Instant.parse(text(dto.createdAt)), dto.readAt?.let(Instant::parse), dto.bookId?.takeIf(String::isNotBlank), dto.categoryId?.takeIf(String::isNotBlank))
    }
    fun notifications(dto: NotificationPageDto): NotificationPage {
        val sequence = requireNotNull(dto.snapshotSequence).also { require(it >= 0) }
        val items = requireNotNull(dto.items).map(::notification).distinctBy { it.id }.also { require(it.all { note -> note.sequence <= sequence }) }
        return NotificationPage(items, dto.nextCursor?.takeIf(String::isNotBlank), sequence, unread(requireNotNull(dto.unread)))
    }
    fun receipt(dto: NotificationReceiptDto) = NotificationReceipt(Instant.parse(text(dto.readAt)), unread(requireNotNull(dto.unread)))
    fun sort(value: CatalogSort) = when (value) {
        CatalogSort.Recommended -> "recommended"
        CatalogSort.Popular -> "popular"
        CatalogSort.Newest -> "newest"
        CatalogSort.Title -> "title_asc"
        CatalogSort.Relevance -> "relevance"
    }
    private fun sort(value: String) = CatalogSort.entries.first { sort(it) == value }
    private fun text(value: String?) = requireNotNull(value).also { require(it.isNotBlank()) }
    private fun nonnegative(value: Int?) = requireNotNull(value).also { require(it >= 0) }
    private fun positive(value: Int?) = requireNotNull(value).also { require(it > 0) }
}
