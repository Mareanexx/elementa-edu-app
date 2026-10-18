package ru.mareanexx.book_impl.presentation.model

import ru.mareanexx.book_api.domain.model.*

data class BookCollectionsState(
    val items: List<BookCollectionChoice> = emptyList(),
    val selected: Set<String> = emptySet(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val failure: BookError? = null,
)
