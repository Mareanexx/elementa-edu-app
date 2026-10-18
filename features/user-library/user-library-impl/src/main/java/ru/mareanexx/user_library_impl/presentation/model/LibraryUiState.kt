package ru.mareanexx.user_library_impl.presentation.model

import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_api.domain.validation.LibrarySearchRules

data class LibraryUiState(
    val ready: Boolean = false,
    val category: LibraryCategory = LibraryCategory.All,
    val input: String = "",
    val appliedSearch: String = "",
    val searchPending: Boolean = false,
    val searchFocused: Boolean = false,
    val recentQueries: List<String> = emptyList(),
    val preferences: LibraryPreferences = LibraryPreferences(),
    val counts: LibraryCounts = LibraryCounts(),
    val countsLoaded: Boolean = false,
    val pages: Map<LibraryCategory, LibraryPageState> = emptyMap(),
    val actionBook: LibraryBook? = null,
    val pendingStatus: LibraryStatusChange? = null,
    val mutating: Boolean = false,
    val sortOpen: Boolean = false,
    val helpOpen: Boolean = false,
    val sortError: LibraryError? = null,
    val operationError: LibraryError? = null,
    val notice: LibraryNotice? = null,
    val noticeVersion: Long = 0,
) {
    val tooShort: Boolean get() = input.isNotBlank() && LibrarySearchRules.normalize(input).length < LibrarySearchRules.MIN_LENGTH
    val searching: Boolean get() = input.isNotBlank()
    fun page(category: LibraryCategory): LibraryPageState = pages[category] ?: LibraryPageState(loading = !ready || !tooShort)
}
