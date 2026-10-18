package ru.mareanexx.user_library_api.domain.model

data class LibraryQuery(
    val category: LibraryCategory = LibraryCategory.All,
    val search: String = "",
    val sort: LibrarySort = LibrarySort.Recent,
)
