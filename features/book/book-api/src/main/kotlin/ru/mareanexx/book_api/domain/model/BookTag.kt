package ru.mareanexx.book_api.domain.model

data class BookTag(
    val id: String,
    val label: String,
    val kind: BookTagKind = BookTagKind.Topic,
)
