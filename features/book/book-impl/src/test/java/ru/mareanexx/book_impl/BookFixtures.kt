package ru.mareanexx.book_impl

import ru.mareanexx.book_api.domain.model.*

internal object BookFixtures {
    fun review(id: String = "r1", mine: Boolean = false) = BookReview(id, "author", "Анна", null, 5, "Полезный разбор реакций", 1_750_000_000_000, null, 3, 1, null, 0, mine)
    fun book() = BookDetails("book_1", "Органическая химия", "Авторы", "Описание", listOf(BookImage("cover", "https://example.test/cover.jpg", "Обложка")), emptyList(),
        160, 48, 5_600_000, 12, BookRatingSummary(4.2, 64, listOf(1, 3, 8, 22, 30)), BookShelves(false, false, 0), BookMetadata(null, 2022, "Русский", null, "EPUB"), previewReviews = listOf(review()))
}
