package ru.mareanexx.book_api.domain.model

data class ReviewsPage(
    val items: List<BookReview>,
    val nextCursor: String?,
    val totalCount: Int,
)
