package ru.mareanexx.book_impl.presentation.model

import ru.mareanexx.book_api.domain.model.*

data class ReviewsUiState(
    val query: ReviewsQuery = ReviewsQuery(),
    val items: List<BookReview> = emptyList(),
    val totalCount: Int = 0,
    val cursor: String? = null,
    val loaded: Boolean = false,
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val failure: BookError? = null,
)
