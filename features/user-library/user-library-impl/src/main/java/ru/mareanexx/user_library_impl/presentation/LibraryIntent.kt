package ru.mareanexx.user_library_impl.presentation

import ru.mareanexx.book_api.domain.model.BookEntry
import ru.mareanexx.user_library_api.domain.model.*

sealed interface LibraryIntent {
    data object Refresh : LibraryIntent
    data object SubmitSearch : LibraryIntent
    data object ClearSearch : LibraryIntent
    data object ClearHistory : LibraryIntent
    data object CloseBookMenu : LibraryIntent
    data object ToggleFavorite : LibraryIntent
    data object ToggleWantToRead : LibraryIntent
    data object AddToCollections : LibraryIntent
    data object CancelStatusChange : LibraryIntent
    data object ConfirmStatusChange : LibraryIntent
    data object OpenSort : LibraryIntent
    data object CloseSort : LibraryIntent
    data object OpenHelp : LibraryIntent
    data object CloseHelp : LibraryIntent
    data object DismissNotice : LibraryIntent
    data object SessionExpired : LibraryIntent
    data class SelectCategory(
        val category: LibraryCategory,
    ) : LibraryIntent
    data class ChangeSearch(
        val value: String,
    ) : LibraryIntent
    data class SearchFocus(
        val focused: Boolean,
    ) : LibraryIntent
    data class LoadMore(
        val category: LibraryCategory,
    ) : LibraryIntent
    data class Retry(
        val category: LibraryCategory,
    ) : LibraryIntent
    data class OpenBook(
        val book: LibraryBook,
        val entry: BookEntry = BookEntry.Details,
    ) : LibraryIntent
    data class ShowBookMenu(
        val book: LibraryBook,
    ) : LibraryIntent
    data class ChangeReadingStatus(
        val status: ReadingStatus,
    ) : LibraryIntent
    data class Sort(
        val value: LibrarySort,
    ) : LibraryIntent
}
