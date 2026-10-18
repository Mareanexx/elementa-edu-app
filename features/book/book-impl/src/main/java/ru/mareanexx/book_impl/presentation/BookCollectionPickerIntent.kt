package ru.mareanexx.book_impl.presentation

internal sealed interface BookCollectionPickerIntent {
    data object Reload : BookCollectionPickerIntent
    data object Save : BookCollectionPickerIntent
    data object Close : BookCollectionPickerIntent
    data class Toggle(
        val id: String,
    ) : BookCollectionPickerIntent
}
