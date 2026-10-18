package ru.mareanexx.user_library_impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_api.domain.repository.BookRepository
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_api.domain.repository.LibraryRepository
import ru.mareanexx.user_library_api.domain.validation.LibrarySearchRules
import ru.mareanexx.user_library_impl.presentation.model.*

class LibraryViewModel(
    private val repository: LibraryRepository,
    private val books: BookRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow(LibraryUiState())
    val state = mutable.asStateFlow()
    private val events = Channel<LibraryEffect>(Channel.BUFFERED)
    val effects = events.receiveAsFlow()
    private val loads = mutableMapOf<LibraryCategory, Job>()
    private var debounce: Job? = null
    private var mutation: Job? = null
    private var revision = 0L
    private var refreshPending = false
    private val initial: Job

    init {
        viewModelScope.launch { repository.recentQueries.collect { values -> mutable.update { it.copy(recentQueries = values) } } }
        initial = viewModelScope.launch {
            when (val result = repository.getPreferences()) {
                is LibraryResult.Success -> mutable.update { it.copy(preferences = result.value, ready = true) }
                is LibraryResult.Failure -> mutable.update { it.copy(ready = true, operationError = result.error) }
            }
            if (mutable.value.operationError != LibraryError.Unauthorized && !mutable.value.searchPending && !mutable.value.tooShort) load(mutable.value.category)
        }
    }

    fun accept(intent: LibraryIntent) {
        if (intent == LibraryIntent.SessionExpired) {
            initial.cancel(); debounce?.cancel(); mutation?.cancel(); cancelLoads()
            revision++
            mutable.value = LibraryUiState(ready = true, preferences = mutable.value.preferences, recentQueries = mutable.value.recentQueries)
            return
        }
        if (intent == LibraryIntent.Refresh && mutable.value.mutating) { refreshPending = true; return }
        if (mutable.value.mutating && intent !in listOf(LibraryIntent.DismissNotice, LibraryIntent.CloseHelp)) return
        when (intent) {
            LibraryIntent.SessionExpired -> Unit
            LibraryIntent.Refresh -> invalidate()
            is LibraryIntent.SelectCategory -> {
                mutable.update { it.copy(category = intent.category) }
                val page = mutable.value.page(intent.category)
                if (mutable.value.ready && !mutable.value.searchPending && !mutable.value.tooShort && (!page.loaded || page.dirty) && loads[intent.category]?.isActive != true) load(intent.category)
            }
            is LibraryIntent.ChangeSearch -> changeSearch(intent.value)
            LibraryIntent.ClearSearch -> changeSearch("")
            LibraryIntent.SubmitSearch -> {
                debounce?.cancel()
                commitSearch()
                rememberSearch()
            }
            is LibraryIntent.SearchFocus -> mutable.update { it.copy(searchFocused = intent.focused) }
            LibraryIntent.ClearHistory -> viewModelScope.launch {
                if (repository.clearRecentQueries() is LibraryResult.Failure) mutable.update { it.copy(operationError = LibraryError.Storage) }
            }.let { }
            is LibraryIntent.LoadMore -> {
                val page = mutable.value.page(intent.category)
                if (!mutable.value.searchPending && !mutable.value.tooShort && page.cursor != null && !page.loading && !page.loadingMore && !page.refreshing && page.failure == null) load(intent.category, append = true)
            }
            is LibraryIntent.Retry -> {
                val page = mutable.value.page(intent.category)
                load(intent.category, append = page.failedAppend && page.failure != LibraryError.Conflict)
            }
            is LibraryIntent.OpenBook -> openBook(intent.book, intent.entry)
            is LibraryIntent.ShowBookMenu -> mutable.update { it.copy(actionBook = intent.book, operationError = null) }
            LibraryIntent.CloseBookMenu -> mutable.update { it.copy(actionBook = null) }
            LibraryIntent.ToggleFavorite -> shelf(favorite = true)
            LibraryIntent.ToggleWantToRead -> shelf(favorite = false)
            LibraryIntent.AddToCollections -> mutable.value.actionBook?.let { openBook(it, BookEntry.Collections) }
            is LibraryIntent.ChangeReadingStatus -> mutable.value.actionBook?.let { book ->
                if (intent.status != ReadingStatus.InProgress) mutable.update { it.copy(actionBook = null, pendingStatus = LibraryStatusChange(book, intent.status), operationError = null) }
            }
            LibraryIntent.CancelStatusChange -> mutable.update { it.copy(pendingStatus = null, operationError = null) }
            LibraryIntent.ConfirmStatusChange -> status()
            LibraryIntent.OpenSort -> mutable.update { it.copy(sortOpen = true, sortError = null) }
            LibraryIntent.CloseSort -> mutable.update { it.copy(sortOpen = false, sortError = null) }
            is LibraryIntent.Sort -> sort(intent.value)
            LibraryIntent.OpenHelp -> mutable.update { it.copy(helpOpen = true) }
            LibraryIntent.CloseHelp -> mutable.update { it.copy(helpOpen = false) }
            LibraryIntent.DismissNotice -> mutable.update { it.copy(notice = null, operationError = null) }
        }
    }

    private fun changeSearch(value: String) {
        debounce?.cancel(); cancelLoads(); revision++
        val input = value.take(LibrarySearchRules.MAX_LENGTH)
        mutable.update { it.copy(input = input, searchPending = true, operationError = null) }
        val normalized = LibrarySearchRules.normalize(input)
        if (normalized.isEmpty()) commitSearch()
        else if (!LibrarySearchRules.canSearch(normalized)) mutable.update { it.copy(searchPending = false) }
        else debounce = viewModelScope.launch { delay(350); commitSearch() }
    }
    private fun commitSearch() {
        val query = LibrarySearchRules.normalize(mutable.value.input)
        if (!LibrarySearchRules.canSearch(query)) return
        val current = mutable.value
        val page = current.page(current.category)
        if (query == current.appliedSearch && (page.loaded && !page.dirty || !current.searchPending && loads[current.category]?.isActive == true)) {
            mutable.update { it.copy(searchPending = false) }
            return
        }
        cancelLoads(); revision++
        mutable.update { it.copy(appliedSearch = query, searchPending = false, pages = emptyMap(), countsLoaded = false) }
        if (mutable.value.ready) load(mutable.value.category)
    }
    private fun rememberSearch() {
        val query = LibrarySearchRules.normalize(mutable.value.input)
        if (query.length >= LibrarySearchRules.MIN_LENGTH) viewModelScope.launch { repository.rememberQuery(query) }
    }
    private fun openBook(book: LibraryBook, entry: BookEntry) {
        rememberSearch()
        mutable.update { it.copy(actionBook = null, searchFocused = false) }
        events.trySend(LibraryEffect.OpenBook(book.id, entry))
    }
    private fun cancelLoads() {
        loads.values.forEach(Job::cancel)
        loads.clear()
        mutable.update { s -> s.copy(pages = s.pages.mapValues { (_, page) -> page.copy(loading = false, refreshing = false, loadingMore = false) }) }
    }
    private fun invalidate() {
        if (!mutable.value.ready || mutable.value.searchPending || mutable.value.tooShort) return
        cancelLoads(); revision++
        mutable.update { s -> s.copy(pages = s.pages.mapValues { (_, page) -> page.copy(dirty = true, cursor = null, failure = null) }) }
        load(mutable.value.category)
    }
    private fun load(category: LibraryCategory, append: Boolean = false) {
        if (!mutable.value.ready || mutable.value.searchPending || mutable.value.tooShort) return
        loads[category]?.cancel()
        val previous = mutable.value.page(category)
        val cursor = if (append) previous.cursor else null
        val query = LibraryQuery(category, mutable.value.appliedSearch, mutable.value.preferences.sort)
        val currentRevision = revision
        updatePage(category) { it.copy(loading = !append && !it.loaded, refreshing = !append && it.loaded, loadingMore = append, failure = null, failedAppend = false) }
        loads[category] = viewModelScope.launch {
            val result = repository.getBooks(query, cursor)
            if (currentRevision != revision) return@launch
            when (result) {
                is LibraryResult.Success -> {
                    updatePage(category) { page -> page.copy(
                        items = (if (append) page.items + result.value.items else result.value.items).distinctBy(LibraryBook::id),
                        cursor = result.value.nextCursor,
                        totalCount = result.value.totalCount,
                        highlights = if (append) page.highlights else result.value.highlights,
                        loaded = true, dirty = false, loading = false, refreshing = false, loadingMore = false, failure = null,
                    ) }
                    mutable.update { it.copy(counts = result.value.counts, countsLoaded = true) }
                }
                is LibraryResult.Failure -> {
                    updatePage(category) { it.copy(loading = false, refreshing = false, loadingMore = false, failure = result.error, failedAppend = append) }
                    if (!append && previous.items.isNotEmpty()) mutable.update { it.copy(operationError = result.error) }
                }
            }
        }
    }
    private fun updatePage(category: LibraryCategory, transform: (LibraryPageState) -> LibraryPageState) {
        mutable.update { it.copy(pages = it.pages + (category to transform(it.pages[category] ?: LibraryPageState()))) }
    }
    private fun shelf(favorite: Boolean) {
        val book = mutable.value.actionBook ?: return
        val selected = if (favorite) !book.shelves.favorite else !book.shelves.wantToRead
        cancelLoads(); revision++
        mutable.update { it.copy(actionBook = null, mutating = true, operationError = null, notice = null) }
        mutation = viewModelScope.launch {
            val result = if (favorite) books.setFavorite(book.id, selected) else books.setWantToRead(book.id, selected)
            when (result) {
                is BookResult.Success -> {
                    patchBook(book.id) { it.copy(shelves = result.value) }
                    mutable.update { it.copy(notice = if (favorite) { if (selected) LibraryNotice.FavoriteAdded else LibraryNotice.FavoriteRemoved } else { if (selected) LibraryNotice.Planned else LibraryNotice.Unplanned }, noticeVersion = it.noticeVersion + 1) }
                    finishMutation(refresh = true)
                }
                is BookResult.Failure -> { mutable.update { it.copy(operationError = mapError(result.error)) }; finishMutation() }
            }
        }
    }
    private fun status() {
        val pending = mutable.value.pendingStatus ?: return
        cancelLoads(); revision++
        mutable.update { it.copy(mutating = true, operationError = null, notice = null) }
        mutation = viewModelScope.launch {
            when (val result = repository.setReadingStatus(pending.book.id, pending.status)) {
                is LibraryResult.Success -> {
                    patchBook(pending.book.id) { result.value }
                    mutable.update { it.copy(pendingStatus = null, notice = if (pending.status == ReadingStatus.Completed) LibraryNotice.Completed else LibraryNotice.Reset, noticeVersion = it.noticeVersion + 1) }
                    finishMutation(refresh = true)
                }
                is LibraryResult.Failure -> { mutable.update { it.copy(operationError = result.error) }; finishMutation() }
            }
        }
    }
    private fun sort(sort: LibrarySort) {
        if (sort == mutable.value.preferences.sort) { mutable.update { it.copy(sortOpen = false) }; return }
        mutable.update { it.copy(mutating = true, sortError = null) }
        mutation = viewModelScope.launch {
            when (val result = repository.savePreferences(LibraryPreferences(sort))) {
                is LibraryResult.Success -> {
                    mutable.update { it.copy(preferences = result.value, sortOpen = false) }
                    finishMutation(refresh = true)
                }
                is LibraryResult.Failure -> { mutable.update { it.copy(sortError = result.error) }; finishMutation() }
            }
        }
    }
    private fun finishMutation(refresh: Boolean = false) {
        mutable.update { it.copy(mutating = false) }
        if (refresh || refreshPending) { refreshPending = false; invalidate() }
    }
    private fun patchBook(id: String, transform: (LibraryBook) -> LibraryBook) {
        fun patch(items: List<LibraryBook>) = items.map { if (it.id == id) transform(it) else it }
        mutable.update { state -> state.copy(pages = state.pages.mapValues { (_, page) -> page.copy(
            items = patch(page.items), highlights = page.highlights?.let { it.copy(inProgress = patch(it.inProgress), completed = patch(it.completed), wantToRead = patch(it.wantToRead)) },
        ) }) }
    }
    private fun mapError(error: BookError): LibraryError = when (error) {
        BookError.Offline -> LibraryError.Offline
        BookError.Timeout -> LibraryError.Timeout
        BookError.Unauthorized -> LibraryError.Unauthorized
        BookError.NotFound, BookError.CollectionNotFound -> LibraryError.NotFound
        BookError.RateLimited -> LibraryError.RateLimited
        BookError.Storage -> LibraryError.Storage
        BookError.Conflict, BookError.Forbidden -> LibraryError.Conflict
        else -> LibraryError.Unavailable
    }
}
