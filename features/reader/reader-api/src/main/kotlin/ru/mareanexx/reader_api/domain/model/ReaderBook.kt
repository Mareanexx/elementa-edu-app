package ru.mareanexx.reader_api.domain.model

import ru.mareanexx.book_api.domain.model.BookShelves
import ru.mareanexx.book_api.domain.model.BookTag

data class ReaderBook(
    val id: String,
    val title: String,
    val authors: String,
    val pages: Int,
    val pagesRead: Int,
    val coverUrl: String?,
    val coverKey: String,
    val ratingAverage: Double?,
    val ratingCount: Int,
    val tags: List<BookTag>,
    val shelves: BookShelves,
)
