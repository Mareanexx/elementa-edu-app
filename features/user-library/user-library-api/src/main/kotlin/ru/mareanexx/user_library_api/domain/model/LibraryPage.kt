package ru.mareanexx.user_library_api.domain.model

data class LibraryPage(
    val items: List<LibraryBook>,
    val nextCursor: String?,
    val totalCount: Int,
    val counts: LibraryCounts,
    val highlights: LibraryHighlights? = null,
)
