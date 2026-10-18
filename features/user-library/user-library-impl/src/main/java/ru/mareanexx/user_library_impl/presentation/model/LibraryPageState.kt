package ru.mareanexx.user_library_impl.presentation.model

import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_api.domain.validation.LibrarySearchRules

data class LibraryPageState(
    val items: List<LibraryBook> = emptyList(),
    val cursor: String? = null,
    val totalCount: Int = 0,
    val highlights: LibraryHighlights? = null,
    val loaded: Boolean = false,
    val dirty: Boolean = false,
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val failure: LibraryError? = null,
    val failedAppend: Boolean = false,
)
