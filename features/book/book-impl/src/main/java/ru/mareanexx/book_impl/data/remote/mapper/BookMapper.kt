package ru.mareanexx.book_impl.data.remote.mapper

import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_impl.data.remote.dto.*
import java.time.Instant

internal object BookMapper {
    fun details(dto: BookDetailsDto): BookDetails {
        val pages = requireNotNull(dto.pages).also { require(it > 0) }
        val meta = requireNotNull(dto.metadata)
        return BookDetails(
            required(dto.id), required(dto.title), required(dto.authors), dto.description.orEmpty(),
            dto.images.orEmpty().take(50).map { BookImage(required(it.id), required(it.url), it.caption.orEmpty()) }.distinctBy { it.id },
            dto.tags.orEmpty().mapNotNull { tag ->
                val id = tag.id?.takeIf(String::isNotBlank)
                val label = tag.label?.takeIf(String::isNotBlank)
                if (id == null || label == null) null else BookTag(id, label, BookTagKind.entries.firstOrNull { it.name.equals(tag.kind, true) } ?: BookTagKind.Topic)
            }.distinctBy { it.id },
            pages, (dto.pagesRead ?: 0).coerceIn(0, pages), (dto.fileBytes ?: 0).coerceAtLeast(0), (dto.notesCount ?: 0).coerceAtLeast(0),
            rating(requireNotNull(dto.rating)), shelves(requireNotNull(dto.shelves)),
            BookMetadata(meta.publisher, meta.year, meta.language, meta.isbn, meta.format.orEmpty()), dto.myReview?.let(::review), dto.previewReviews.orEmpty().map(::review),
        )
    }
    fun rating(dto: BookRatingDto): BookRatingSummary {
        val count = requireNotNull(dto.count).also { require(it >= 0) }
        val distribution = requireNotNull(dto.distribution).also { require(it.size == 5 && it.all { n -> n >= 0 } && it.sumOf(Int::toLong) == count.toLong()) }
        val average = if (count == 0) null else requireNotNull(dto.average).also { require(it.isFinite() && it in 1.0..5.0) }
        return BookRatingSummary(average, count, distribution)
    }
    fun shelves(dto: BookShelvesDto) = BookShelves(requireNotNull(dto.favorite), requireNotNull(dto.wantToRead), requireNotNull(dto.collectionCount).also { require(it in 0..30) })
    fun review(dto: BookReviewDto) = BookReview(
        required(dto.id), required(dto.authorId), required(dto.authorName), dto.avatarUrl, requireNotNull(dto.rating).also { require(it in 1..5) }, dto.text.orEmpty(),
        Instant.parse(required(dto.createdAt)).toEpochMilli(), dto.updatedAt?.let { Instant.parse(it).toEpochMilli() },
        nonnegative(dto.likes), nonnegative(dto.dislikes), reaction(dto.reaction), nonnegative(dto.replyCount), requireNotNull(dto.isMine),
    )
    fun reply(dto: BookReplyDto) = ReviewReply(required(dto.id), required(dto.authorName), dto.avatarUrl, required(dto.text), Instant.parse(required(dto.createdAt)).toEpochMilli(), requireNotNull(dto.isMine))
    fun reviews(dto: BookReviewsPageDto) = ReviewsPage(requireNotNull(dto.items).map(::review).distinctBy { it.id }, dto.nextCursor?.takeIf(String::isNotBlank), nonnegative(dto.totalCount))
    fun replies(dto: BookRepliesPageDto) = RepliesPage(requireNotNull(dto.items).map(::reply).distinctBy { it.id }, dto.nextCursor?.takeIf(String::isNotBlank), nonnegative(dto.totalCount))
    fun collections(dto: BookCollectionsDto) = requireNotNull(dto.items).map { BookCollectionChoice(required(it.id), required(it.name), nonnegative(it.bookCount), requireNotNull(it.selected)) }.also { require(it.size <= 30 && it.distinctBy { v -> v.id }.size == it.size) }
    fun sort(value: ReviewSort) = when (value) { ReviewSort.Helpful -> "helpful"; ReviewSort.Newest -> "newest"; ReviewSort.Highest -> "rating_desc"; ReviewSort.Lowest -> "rating_asc" }
    fun reaction(value: ReviewReaction?) = value?.name?.lowercase(java.util.Locale.ROOT)
    private fun reaction(value: String?) = when (value) { null -> null; "like" -> ReviewReaction.Like; "dislike" -> ReviewReaction.Dislike; else -> error("Неизвестная реакция") }
    private fun required(value: String?) = requireNotNull(value).also { require(it.isNotBlank()) }
    private fun nonnegative(value: Int?) = requireNotNull(value).also { require(it >= 0) }
}
