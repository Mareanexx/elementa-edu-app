package ru.mareanexx.book_api.domain.model

data class BookDetails(
    val id: String,
    val title: String,
    val authors: String,
    val description: String,
    val images: List<BookImage>,
    val tags: List<BookTag>,
    val pages: Int,
    val pagesRead: Int,
    val fileBytes: Long,
    val notesCount: Int,
    val rating: BookRatingSummary,
    val shelves: BookShelves,
    val metadata: BookMetadata,
    val myReview: BookReview? = null,
    val previewReviews: List<BookReview> = emptyList(),
)
