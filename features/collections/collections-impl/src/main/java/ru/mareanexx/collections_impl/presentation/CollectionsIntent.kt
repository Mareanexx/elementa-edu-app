package ru.mareanexx.collections_impl.presentation

import ru.mareanexx.collections_api.domain.model.*

sealed interface CollectionsIntent {
    data class OpenBook(
        val id: String,
    ) : CollectionsIntent
    data object SessionExpired : CollectionsIntent
    data object Reload : CollectionsIntent
    data object OpenAll : CollectionsIntent
    data object Back : CollectionsIntent
    data object Create : CollectionsIntent
    data object Save : CollectionsIntent
    data object DismissEditor : CollectionsIntent
    data object ConfirmDelete : CollectionsIntent
    data object DismissDelete : CollectionsIntent
    data object OpenSettings : CollectionsIntent
    data object CloseSettings : CollectionsIntent
    data object OpenSort : CollectionsIntent
    data object CloseSort : CollectionsIntent
    data object ReloadBooks : CollectionsIntent
    data object OpenPicker : CollectionsIntent
    data object ReloadPicker : CollectionsIntent
    data object ClosePicker : CollectionsIntent
    data object AddBooks : CollectionsIntent
    data object ConfirmRemoveBook : CollectionsIntent
    data object DismissRemoveBook : CollectionsIntent
    data object DismissNotice : CollectionsIntent
    data class OpenCollection(
        val id: String,
    ) : CollectionsIntent
    data class Edit(
        val id: String,
    ) : CollectionsIntent
    data class Delete(
        val id: String,
    ) : CollectionsIntent
    data class ChangeName(
        val value: String,
    ) : CollectionsIntent
    data class ChangeCover(
        val value: CollectionCover,
    ) : CollectionsIntent
    data class SetPreferences(
        val value: CollectionsPreferences,
    ) : CollectionsIntent
    data class SearchBooks(
        val query: String,
    ) : CollectionsIntent
    data class ToggleBook(
        val id: String,
    ) : CollectionsIntent
    data class RemoveBook(
        val id: String,
    ) : CollectionsIntent
    data object BeginCoverImport : CollectionsIntent
    data object CoverImportFailed : CollectionsIntent
    data object UndoRemoveBook : CollectionsIntent
    data class UploadCover(
        val value: CollectionCoverUpload,
    ) : CollectionsIntent
    data class SwipeRemoveBook(
        val id: String,
    ) : CollectionsIntent
}
