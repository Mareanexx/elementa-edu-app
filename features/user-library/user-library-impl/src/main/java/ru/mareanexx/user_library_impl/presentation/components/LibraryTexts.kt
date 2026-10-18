package ru.mareanexx.user_library_impl.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import ru.mareanexx.user_library_impl.R
import ru.mareanexx.user_library_api.domain.model.*

@Composable
internal fun categoryText(category: LibraryCategory): String = LocalResources.current.getString(when (category) {
    LibraryCategory.All -> R.string.library_all
    LibraryCategory.InProgress -> R.string.library_in_progress
    LibraryCategory.Completed -> R.string.library_completed
    LibraryCategory.WantToRead -> R.string.library_want
    LibraryCategory.Downloaded -> R.string.library_downloaded
})
@Composable
internal fun sortText(sort: LibrarySort): String = LocalResources.current.getString(when (sort) {
    LibrarySort.Recent -> R.string.library_sort_recent
    LibrarySort.Title -> R.string.library_sort_title
    LibrarySort.Author -> R.string.library_sort_author
})
@Composable
internal fun libraryError(error: LibraryError): String = LocalResources.current.getString(when (error) {
    LibraryError.Offline -> R.string.library_error_offline
    LibraryError.Timeout -> R.string.library_error_timeout
    LibraryError.Unauthorized -> R.string.library_error_session
    LibraryError.NotFound -> R.string.library_error_not_found
    LibraryError.Unavailable -> R.string.library_error_unavailable
    LibraryError.RateLimited -> R.string.library_error_rate
    LibraryError.InvalidResponse -> R.string.library_error_response
    LibraryError.Conflict -> R.string.library_error_conflict
    LibraryError.Storage -> R.string.library_error_storage
})
internal fun ratingText(value: Double): String = String.format(java.util.Locale.forLanguageTag("ru"), "%.1f", value)
