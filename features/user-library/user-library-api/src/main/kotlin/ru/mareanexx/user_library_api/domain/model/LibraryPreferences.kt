package ru.mareanexx.user_library_api.domain.model

data class LibraryPreferences(
    val sort: LibrarySort = LibrarySort.Recent,
)
