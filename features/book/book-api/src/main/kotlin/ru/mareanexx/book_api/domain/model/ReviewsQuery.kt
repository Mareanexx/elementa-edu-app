package ru.mareanexx.book_api.domain.model

data class ReviewsQuery(
    val sort: ReviewSort = ReviewSort.Helpful,
    val rating: Int? = null,
)
