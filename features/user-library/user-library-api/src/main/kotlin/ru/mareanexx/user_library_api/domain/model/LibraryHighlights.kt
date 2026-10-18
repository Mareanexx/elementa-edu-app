package ru.mareanexx.user_library_api.domain.model

data class LibraryHighlights(
    val inProgress: List<LibraryBook> = emptyList(),
    val completed: List<LibraryBook> = emptyList(),
    val wantToRead: List<LibraryBook> = emptyList(),
)
