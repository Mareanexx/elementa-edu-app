package ru.mareanexx.book_impl.presentation.model

import ru.mareanexx.book_api.domain.model.*

data class ReviewDraft(
    val rating: Int = 0,
    val text: String = "",
    val saving: Boolean = false,
    val fieldError: BookError? = null,
    val requestError: BookError? = null,
)
