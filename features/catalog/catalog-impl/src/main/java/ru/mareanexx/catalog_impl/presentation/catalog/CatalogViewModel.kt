package ru.mareanexx.catalog_impl.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_api.domain.repository.CatalogRepository
import ru.mareanexx.catalog_impl.presentation.CatalogEffect
import ru.mareanexx.catalog_impl.presentation.model.*

internal class CatalogViewModel(
    private val repository: CatalogRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow(CatalogUiState())
    val state = mutable.asStateFlow()
    private val events = Channel<CatalogEffect>(Channel.BUFFERED)
    val effects = events.receiveAsFlow()
    private val jobs = mutableMapOf<String, Job>()
    private var categoryJob: Job? = null
    private var generation = 0
    private val dirty = mutableSetOf<String>()
    fun accept(intent: CatalogIntent) {
        when (intent) {
            is CatalogIntent.Open -> {
                if (state.value.sort != intent.sort) reset(intent.sort)
                mutable.update { it.copy(selectedId = intent.categoryId) }
                categories(force = state.value.categoryError != null); load(force = intent.categoryId.orEmpty() in dirty)
            }
            is CatalogIntent.Select -> { mutable.update { it.copy(selectedId = intent.id) }; if (!state.value.page.loaded || intent.id.orEmpty() in dirty) load(force = intent.id.orEmpty() in dirty) }
            is CatalogIntent.Sort -> { if (state.value.sort != intent.value) { reset(intent.value); load() } }
            CatalogIntent.Refresh -> { categories(force = state.value.categoryError != null); load(force = true) }
            CatalogIntent.More -> load(append = true)
            CatalogIntent.Retry -> { categories(force = state.value.categoryError != null); load(append = state.value.page.appendFailed && state.value.page.error != CatalogError.Conflict, force = true) }
        }
    }
    fun invalidate() {
        if (state.value.pages.isEmpty()) return
        generation++; jobs.values.forEach(Job::cancel); jobs.clear()
        dirty.addAll(state.value.pages.keys)
        mutable.update { current -> current.copy(pages = current.pages.mapValues { (_, page) -> page.copy(loading = false, refreshing = false, loadingMore = false) }) }
        load(force = true)
    }
    private fun reset(sort: CatalogSort) {
        generation++
        dirty.clear()
        jobs.values.forEach(Job::cancel); jobs.clear()
        mutable.update { it.copy(sort = sort, pages = emptyMap()) }
    }
    private fun categories(force: Boolean = false) {
        if (categoryJob?.isActive == true || (state.value.categoriesLoaded && !force)) return
        categoryJob = viewModelScope.launch {
            when (val result = repository.categories()) {
                is CatalogResult.Success -> mutable.update { it.copy(categories = result.value, categoriesLoaded = true, categoryError = null) }
                is CatalogResult.Failure -> { mutable.update { it.copy(categoryError = result.error) }; expired(result.error) }
            }
        }
    }
    private fun update(key: String, transform: (BookPageState) -> BookPageState) = mutable.update { it.copy(pages = it.pages + (key to transform(it.pages[key] ?: BookPageState()))) }
    private fun load(append: Boolean = false, force: Boolean = false) {
        val key = state.value.selectedId.orEmpty()
        val page = state.value.page
        if (jobs[key]?.isActive == true || (append && (page.nextCursor == null || page.appendFailed && !force))) return
        if (!append && page.loaded && !force) return
        val token = generation
        val query = CatalogQuery(state.value.selectedId, sort = state.value.sort)
        update(key) { it.copy(loading = !it.loaded, refreshing = it.loaded && !append, loadingMore = append, error = null, appendFailed = false) }
        jobs[key] = viewModelScope.launch {
            val result = repository.books(query, if (append) page.nextCursor else null)
            if (generation != token) return@launch
            when (result) {
                is CatalogResult.Success -> { dirty.remove(key); update(key) { it.copy(items = (if (append) it.items + result.value.items else result.value.items).distinctBy { book -> book.id }, loaded = true, loading = false, refreshing = false, loadingMore = false, total = result.value.total, nextCursor = result.value.nextCursor) } }
                is CatalogResult.Failure -> { update(key) { it.copy(loading = false, refreshing = false, loadingMore = false, error = result.error, appendFailed = append) }; expired(result.error) }
            }
        }
    }
    private fun expired(error: CatalogError) { if (error == CatalogError.Unauthorized) events.trySend(CatalogEffect.SessionExpired) }
}
