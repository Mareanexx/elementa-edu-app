package ru.mareanexx.user_library_api.domain.model

import ru.mareanexx.book_api.domain.model.BookTag
import ru.mareanexx.book_api.domain.model.BookShelves

data class LibraryBook(
    val id: String,
    val title: String,
    val authors: String,
    val pages: Int,
    val pagesRead: Int,
    val status: ReadingStatus,
    val coverUrl: String?,
    val coverKey: String,
    val rating: LibraryRating?,
    val tags: List<BookTag>,
    val shelves: BookShelves,
)
