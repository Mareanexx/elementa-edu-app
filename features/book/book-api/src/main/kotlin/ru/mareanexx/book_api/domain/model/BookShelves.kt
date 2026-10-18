package ru.mareanexx.book_api.domain.model

data class BookShelves(
    val favorite: Boolean,
    val wantToRead: Boolean,
    val collectionCount: Int,
)
