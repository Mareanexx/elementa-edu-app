package ru.mareanexx.book_impl.presentation.model

import ru.mareanexx.book_api.domain.model.*

data class ReviewThreadState(
    val review: BookReview,
    val items: List<ReviewReply> = emptyList(),
    val totalCount: Int = 0,
    val cursor: String? = null,
    val loaded: Boolean = false,
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val failure: BookError? = null,
    val draft: String = "",
    val posting: Boolean = false,
    val fieldError: BookError? = null,
    val postError: BookError? = null,
    val requestKey: String? = null,
    val deleteId: String? = null,
    val deleting: Boolean = false,
)
