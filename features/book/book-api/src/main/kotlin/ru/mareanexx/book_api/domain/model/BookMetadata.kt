package ru.mareanexx.book_api.domain.model

data class BookMetadata(
    val publisher: String?,
    val year: Int?,
    val language: String?,
    val isbn: String?,
    val format: String,
)
