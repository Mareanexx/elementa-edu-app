package ru.mareanexx.catalog_impl

import ru.mareanexx.book_api.domain.model.BookShelves
import ru.mareanexx.catalog_api.domain.model.*
import java.time.Instant

internal object CatalogFixtures {
    fun book(id: String = "book_1") = CatalogBook(id, "Органическая химия", "Мария Белова", 160, 48, null, "organic", CatalogRating(4.5, 8), emptyList(), BookShelves(false, false, 0))
    fun category(id: String = "organic") = CatalogCategory(id, "Органическая", "Описание", id, 2)
    fun page(id: String = "book_1", cursor: String? = null) = CatalogPage(listOf(book(id)), cursor, 2)
    fun home() = HomeFeed(listOf(book()), listOf(category()), listOf(HomeSection("popular", listOf(book()), CatalogSort.Popular)), 12, 5, UnreadState(3, 1))
    fun note(id: String = "notice_1", sequence: Long = 1) = CatalogNotification(id, sequence, "Новая книга", "Описание книги", Instant.parse("2026-10-18T12:00:00Z"), null, "book_1", null)
}
