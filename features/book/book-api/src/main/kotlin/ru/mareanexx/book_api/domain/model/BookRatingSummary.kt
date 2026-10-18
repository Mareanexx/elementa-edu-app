package ru.mareanexx.book_api.domain.model

data class BookRatingSummary(
    val average: Double?,
    val count: Int,
    val distribution: List<Int>,
)
