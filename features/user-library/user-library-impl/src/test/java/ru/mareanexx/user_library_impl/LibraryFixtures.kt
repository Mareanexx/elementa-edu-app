package ru.mareanexx.user_library_impl

import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.user_library_api.domain.model.*

internal object LibraryFixtures {
    fun book(id: String = "book_1") = LibraryBook(id, "Органическая химия", "Мария Белова", 160, 48, ReadingStatus.InProgress, null, "chemistry", LibraryRating(4.5, 10), emptyList(), BookShelves(false, false, 0))
    fun page(items: List<LibraryBook> = listOf(book()), cursor: String? = null) = LibraryPage(items, cursor, 2, LibraryCounts(2, 2, 0, 0))
}
