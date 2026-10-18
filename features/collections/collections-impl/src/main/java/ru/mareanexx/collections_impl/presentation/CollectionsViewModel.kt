package ru.mareanexx.collections_impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.mareanexx.collections_api.domain.model.*
import ru.mareanexx.collections_api.domain.repository.CollectionsRepository
import ru.mareanexx.collections_api.domain.usecase.SaveCollectionUseCase
import ru.mareanexx.collections_api.domain.validation.CollectionRules
import ru.mareanexx.collections_impl.presentation.model.*

class CollectionsViewModel(
    private val repository: CollectionsRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow(CollectionsUiState())
    val state = mutable.asStateFlow()
    private val events = Channel<CollectionsEffect>(Channel.BUFFERED)
    val effects = events.receiveAsFlow()
    private var listJob: Job? = null
    private var booksJob: Job? = null
    private var pickerJob: Job? = null
    private var coverJob: Job? = null
    private val saveCollection = SaveCollectionUseCase(repository)
    init {
        viewModelScope.launch { repository.preferences.collect { value -> mutable.update { it.copy(preferences = value) } } }
        load()
    }
    fun accept(intent: CollectionsIntent) {
        val current = mutable.value
        if (intent == CollectionsIntent.SessionExpired) {
            listJob?.cancel()
            booksJob?.cancel()
            pickerJob?.cancel()
            coverJob?.cancel()
            mutable.value = CollectionsUiState(loading = false, preferences = current.preferences)
            return
        }
        if (current.busy && intent != CollectionsIntent.DismissNotice) return
        if (current.editor?.coverBusy == true && intent !is CollectionsIntent.UploadCover && intent != CollectionsIntent.CoverImportFailed && intent != CollectionsIntent.DismissNotice) return
        when (intent) {
            is CollectionsIntent.OpenBook -> if (current.books.any { it.id == intent.id }) events.trySend(CollectionsEffect.OpenBook(intent.id))
            CollectionsIntent.SessionExpired -> Unit
            CollectionsIntent.Reload -> if (!current.loading && !current.refreshing) load()
            CollectionsIntent.OpenAll -> mutable.update { it.copy(page = CollectionsPage.All) }
            is CollectionsIntent.OpenCollection -> if (current.items.any { it.id == intent.id }) {
                mutable.update { it.copy(page = CollectionsPage.Detail, returnPage = if (it.page == CollectionsPage.Detail) it.returnPage else it.page, selectedId = intent.id, books = emptyList(), booksLoaded = false, booksRefreshing = false, booksFailure = null) }
                loadBooks(intent.id)
            }
            CollectionsIntent.Back -> {
                booksJob?.cancel()
                mutable.update { it.copy(page = if (it.page == CollectionsPage.Detail) it.returnPage else CollectionsPage.Overview, selectedId = null, books = emptyList(), booksFailure = null, booksLoading = false, booksRefreshing = false, booksLoaded = false) }
            }
            CollectionsIntent.Create -> if (current.customCount >= CollectionRules.MAX_COLLECTIONS) mutable.update { it.copy(notice = CollectionsNotice.LimitReached, noticeVersion = it.noticeVersion + 1) }
                else mutable.update { it.copy(editor = CollectionEditorState()) }
            is CollectionsIntent.Edit -> current.items.firstOrNull { it.id == intent.id && !it.isSystem }?.let { item -> mutable.update { it.copy(editor = CollectionEditorState(item.id, CollectionForm(item.name, item.cover, item.image))) } }
            is CollectionsIntent.ChangeName -> mutable.update { it.copy(editor = it.editor?.copy(form = it.editor.form.copy(name = intent.value), fieldError = null, requestError = null)) }
            is CollectionsIntent.ChangeCover -> mutable.update { it.copy(editor = it.editor?.copy(form = it.editor.form.copy(cover = intent.value, image = null), requestError = null)) }
            CollectionsIntent.Save -> save()
            CollectionsIntent.DismissEditor -> mutable.update { it.copy(editor = null) }
            is CollectionsIntent.Delete -> if (current.items.any { it.id == intent.id && !it.isSystem }) mutable.update { it.copy(deleteId = intent.id, operationError = null) }
            CollectionsIntent.ConfirmDelete -> delete()
            CollectionsIntent.DismissDelete -> mutable.update { it.copy(deleteId = null, operationError = null) }
            CollectionsIntent.OpenSettings -> mutable.update { it.copy(settingsOpen = true) }
            CollectionsIntent.CloseSettings -> mutable.update { it.copy(settingsOpen = false) }
            CollectionsIntent.OpenSort -> mutable.update { it.copy(sortOpen = true) }
            CollectionsIntent.CloseSort -> mutable.update { it.copy(sortOpen = false) }
            is CollectionsIntent.SetPreferences -> preferences(intent.value)
            CollectionsIntent.ReloadBooks -> current.selectedId?.let(::loadBooks)
            CollectionsIntent.OpenPicker -> if (current.picker == null && current.page == CollectionsPage.Detail && !current.booksLoading) {
                mutable.update { it.copy(picker = CollectionPickerState(existing = it.books.map(CollectionBook::id).toSet())) }
                loadPicker()
            }
            CollectionsIntent.ReloadPicker -> loadPicker()
            CollectionsIntent.ClosePicker -> { pickerJob?.cancel(); mutable.update { it.copy(picker = null) } }
            is CollectionsIntent.SearchBooks -> mutable.update { it.copy(picker = it.picker?.copy(query = intent.query)) }
            is CollectionsIntent.ToggleBook -> mutable.update { state ->
                val picker = state.picker ?: return@update state
                if (intent.id in picker.existing || picker.books.none { it.id == intent.id }) state
                else state.copy(picker = picker.copy(selected = if (intent.id in picker.selected) picker.selected - intent.id else picker.selected + intent.id, error = null))
            }
            CollectionsIntent.AddBooks -> addBooks()
            is CollectionsIntent.RemoveBook -> if (current.books.any { it.id == intent.id }) mutable.update { it.copy(removeBookId = intent.id, operationError = null) }
            CollectionsIntent.ConfirmRemoveBook -> removeBook()
            CollectionsIntent.DismissRemoveBook -> mutable.update { it.copy(removeBookId = null, operationError = null) }
            CollectionsIntent.BeginCoverImport -> mutable.update { it.copy(editor = it.editor?.copy(coverBusy = true, requestError = null)) }
            is CollectionsIntent.UploadCover -> uploadCover(intent.value)
            CollectionsIntent.CoverImportFailed -> mutable.update { it.copy(editor = it.editor?.copy(coverBusy = false, requestError = CollectionsError.InvalidImage)) }
            is CollectionsIntent.SwipeRemoveBook -> removeBook(intent.id)
            CollectionsIntent.UndoRemoveBook -> undoRemoval()
            CollectionsIntent.DismissNotice -> mutable.update { it.copy(notice = null, undoRemoval = null, operationError = if (it.deleteId == null && it.removeBookId == null) null else it.operationError) }
        }
    }
    private fun load() {
        listJob?.cancel()
        mutable.update { it.copy(loading = it.items.isEmpty(), refreshing = it.items.isNotEmpty(), failure = null) }
        listJob = viewModelScope.launch {
            when (val result = repository.getCollections()) {
                is CollectionsResult.Success -> mutable.update { it.copy(items = result.value, loading = false, refreshing = false) }
                is CollectionsResult.Failure -> mutable.update { it.copy(failure = result.error, loading = false, refreshing = false) }
            }
        }
    }
    private fun loadBooks(id: String) {
        booksJob?.cancel()
        mutable.update { it.copy(booksLoading = !it.booksLoaded, booksRefreshing = it.booksLoaded, booksFailure = null) }
        booksJob = viewModelScope.launch {
            when (val result = repository.getBooks(id)) {
                is CollectionsResult.Success -> if (mutable.value.selectedId == id) mutable.update { it.copy(books = result.value, booksLoading = false, booksRefreshing = false, booksLoaded = true) }
                is CollectionsResult.Failure -> if (mutable.value.selectedId == id) mutable.update { it.copy(booksFailure = result.error, booksLoading = false, booksRefreshing = false) }
            }
        }
    }
    private fun save() {
        val editor = mutable.value.editor ?: return
        CollectionRules.validate(editor.form.name)?.let { error -> mutable.update { it.copy(editor = editor.copy(fieldError = error)) }; return }
        mutable.update { it.copy(busy = true, editor = editor.copy(fieldError = null, requestError = null)) }
        viewModelScope.launch {
            when (val result = saveCollection(editor.id, editor.form)) {
                is CollectionsResult.Success -> mutable.update {
                    if (editor.id == null) it.copy(items = it.items + result.value, editor = null, busy = false,
                        page = CollectionsPage.Detail, returnPage = if (it.page == CollectionsPage.Detail) it.returnPage else it.page, selectedId = result.value.id,
                        books = emptyList(), booksLoading = false, booksRefreshing = false, booksLoaded = true, booksFailure = null, notice = CollectionsNotice.Created, noticeVersion = it.noticeVersion + 1)
                    else it.copy(items = it.items.map { old -> if (old.id == result.value.id) result.value else old }, editor = null, busy = false, notice = CollectionsNotice.Updated, noticeVersion = it.noticeVersion + 1)
                }
                is CollectionsResult.Failure -> mutable.update {
                    val field = result.error in listOf(CollectionsError.NameRequired, CollectionsError.NameTooLong, CollectionsError.NameTaken)
                    it.copy(busy = false, editor = editor.copy(fieldError = if (field) result.error else null, requestError = if (field) null else result.error))
                }
            }
        }
    }
    private fun delete() {
        val id = mutable.value.deleteId ?: return
        mutable.update { it.copy(busy = true, operationError = null) }
        viewModelScope.launch {
            when (val result = repository.delete(id)) {
                is CollectionsResult.Success -> mutable.update {
                    it.copy(items = it.items.filter { item -> item.id != id }, deleteId = null, busy = false, notice = CollectionsNotice.Deleted, noticeVersion = it.noticeVersion + 1,
                        page = if (it.selectedId == id) it.returnPage else it.page, selectedId = if (it.selectedId == id) null else it.selectedId)
                }
                is CollectionsResult.Failure -> mutable.update { it.copy(busy = false, operationError = result.error) }
            }
        }
    }
    private fun loadPicker() {
        if (mutable.value.picker == null) return
        pickerJob?.cancel()
        mutable.update { it.copy(picker = it.picker?.copy(loading = true, error = null)) }
        pickerJob = viewModelScope.launch {
            when (val result = repository.getLibrary()) {
                is CollectionsResult.Success -> mutable.update { it.copy(picker = it.picker?.copy(books = result.value, loading = false)) }
                is CollectionsResult.Failure -> mutable.update { it.copy(picker = it.picker?.copy(error = result.error, loading = false)) }
            }
        }
    }
    private fun addBooks() {
        val id = mutable.value.selectedId ?: return
        val ids = mutable.value.picker?.selected?.takeIf { it.isNotEmpty() } ?: return
        val added = mutable.value.picker!!.books.filter { it.id in ids }
        mutable.update { it.copy(busy = true, picker = it.picker?.copy(error = null)) }
        viewModelScope.launch {
            when (val result = repository.addBooks(id, ids)) {
                is CollectionsResult.Success -> { mutable.update { it.copy(busy = false, picker = null, books = (it.books + added).distinctBy(CollectionBook::id), notice = CollectionsNotice.BooksAdded, noticeVersion = it.noticeVersion + 1) }; loadBooks(id); load() }
                is CollectionsResult.Failure -> mutable.update { it.copy(busy = false, picker = it.picker?.copy(error = result.error)) }
            }
        }
    }
    private fun removeBook(swipedId: String? = null) {
        val current = mutable.value
        val id = current.selectedId ?: return
        val bookId = swipedId ?: current.removeBookId ?: return
        val index = current.books.indexOfFirst { it.id == bookId }.takeIf { it >= 0 } ?: return
        val removed = RemovedCollectionBook(id, current.books[index], index)
        listJob?.cancel()
        booksJob?.cancel()
        mutable.update { it.copy(busy = true, operationError = null, removeBookId = null, undoRemoval = null, notice = null,
            books = it.books.filter { book -> book.id != bookId }, refreshing = false, booksRefreshing = false,
            items = it.items.map { item -> if (item.id == id) item.copy(bookCount = (item.bookCount - 1).coerceAtLeast(0)) else item }) }
        viewModelScope.launch {
            when (val result = repository.removeBook(id, bookId)) {
                is CollectionsResult.Success -> {
                    mutable.update { it.copy(busy = false, undoRemoval = removed, notice = CollectionsNotice.BookRemoved, noticeVersion = it.noticeVersion + 1) }
                    load()
                }
                is CollectionsResult.Failure -> mutable.update {
                    it.copy(busy = false, operationError = result.error,
                        books = if (it.selectedId == id) restoreBook(it.books, removed) else it.books,
                        items = it.items.map { item -> if (item.id == id) item.copy(bookCount = item.bookCount + 1) else item })
                }
            }
        }
    }
    private fun undoRemoval() {
        val removed = mutable.value.undoRemoval ?: return
        mutable.update { it.copy(busy = true, operationError = null, notice = null) }
        viewModelScope.launch {
            when (val result = repository.addBooks(removed.collectionId, setOf(removed.book.id))) {
                is CollectionsResult.Success -> {
                    mutable.update { it.copy(busy = false, undoRemoval = null,
                        books = if (it.selectedId == removed.collectionId) restoreBook(it.books, removed) else it.books,
                        notice = CollectionsNotice.BookRestored, noticeVersion = it.noticeVersion + 1) }
                    load()
                }
                is CollectionsResult.Failure -> mutable.update { it.copy(busy = false, operationError = result.error) }
            }
        }
    }
    private fun restoreBook(books: List<CollectionBook>, removed: RemovedCollectionBook): List<CollectionBook> =
        if (books.any { it.id == removed.book.id }) books else books.toMutableList().apply { add(removed.index.coerceIn(0, size), removed.book) }

    private fun uploadCover(upload: CollectionCoverUpload) {
        if (mutable.value.editor == null || coverJob?.isActive == true) return
        mutable.update { it.copy(editor = it.editor?.copy(coverBusy = true, requestError = null)) }
        coverJob = viewModelScope.launch {
            when (val result = repository.uploadCover(upload)) {
                is CollectionsResult.Success -> mutable.update { it.copy(editor = it.editor?.copy(coverBusy = false, form = it.editor.form.copy(image = result.value))) }
                is CollectionsResult.Failure -> mutable.update { it.copy(editor = it.editor?.copy(coverBusy = false, requestError = result.error)) }
            }
        }
    }
    private fun preferences(value: CollectionsPreferences) {
        if (mutable.value.preferencesSaving) return
        mutable.update { it.copy(preferencesSaving = true, operationError = null) }
        viewModelScope.launch {
            when (val result = repository.setPreferences(value)) {
                is CollectionsResult.Success -> mutable.update { it.copy(preferences = value, preferencesSaving = false, sortOpen = false) }
                is CollectionsResult.Failure -> mutable.update { it.copy(preferencesSaving = false, operationError = result.error) }
            }
        }
    }
}
