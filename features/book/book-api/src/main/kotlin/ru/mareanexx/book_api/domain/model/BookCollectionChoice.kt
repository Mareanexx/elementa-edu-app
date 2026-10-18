package ru.mareanexx.book_api.domain.model

data class BookCollectionChoice(
    val id: String,
    val name: String,
    val bookCount: Int,
    val selected: Boolean,
)
