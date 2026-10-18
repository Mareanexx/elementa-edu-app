package ru.mareanexx.user_library_impl.presentation

import ru.mareanexx.book_api.domain.model.BookEntry

sealed interface LibraryEffect {
    data class OpenBook(
        val id: String,
        val entry: BookEntry,
    ) : LibraryEffect
}
