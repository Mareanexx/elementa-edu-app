package ru.mareanexx.catalog_api.domain.model

import ru.mareanexx.book_api.domain.model.BookShelves
import ru.mareanexx.book_api.domain.model.BookTag

data class CatalogBook(
    val id: String,
    val title: String,
    val authors: String,
    val pages: Int,
    val pagesRead: Int,
    val coverUrl: String?,
    val coverKey: String,
    val rating: CatalogRating?,
    val tags: List<BookTag>,
    val shelves: BookShelves,
)
