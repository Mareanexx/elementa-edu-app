package ru.mareanexx.collections_impl.presentation.model

import ru.mareanexx.collections_api.domain.model.*

data class CollectionsUiState(
    val items: List<BookCollection> = emptyList(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val failure: CollectionsError? = null,
    val preferences: CollectionsPreferences = CollectionsPreferences(),
    val preferencesSaving: Boolean = false,
    val page: CollectionsPage = CollectionsPage.Overview,
    val returnPage: CollectionsPage = CollectionsPage.Overview,
    val selectedId: String? = null,
    val books: List<CollectionBook> = emptyList(),
    val booksLoading: Boolean = false,
    val booksRefreshing: Boolean = false,
    val booksLoaded: Boolean = false,
    val booksFailure: CollectionsError? = null,
    val editor: CollectionEditorState? = null,
    val deleteId: String? = null,
    val removeBookId: String? = null,
    val picker: CollectionPickerState? = null,
    val undoRemoval: RemovedCollectionBook? = null,
    val settingsOpen: Boolean = false,
    val sortOpen: Boolean = false,
    val busy: Boolean = false,
    val operationError: CollectionsError? = null,
    val notice: CollectionsNotice? = null,
    val noticeVersion: Long = 0,
) {
    val selected: BookCollection? get() = items.firstOrNull { it.id == selectedId }
    val customCount: Int get() = items.count { !it.isFavorite }
}
