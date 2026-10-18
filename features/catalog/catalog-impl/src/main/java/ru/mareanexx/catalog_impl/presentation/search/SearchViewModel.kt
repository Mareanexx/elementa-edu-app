package ru.mareanexx.catalog_impl.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_api.domain.repository.CatalogRepository
import ru.mareanexx.catalog_api.domain.validation.CatalogSearchRules
import ru.mareanexx.catalog_impl.presentation.CatalogEffect
import ru.mareanexx.catalog_impl.presentation.model.*

internal class SearchViewModel(
    private val repository: CatalogRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow(SearchUiState())
    val state = mutable.asStateFlow()
    private val events = Channel<CatalogEffect>(Channel.BUFFERED)
    val effects = events.receiveAsFlow()
    private var job: Job? = null
    private var suggestionJob: Job? = null
    private var generation = 0
    init { viewModelScope.launch { repository.recentQueries.collect { queries -> mutable.update { it.copy(history = queries) } } } }
    fun accept(intent: SearchIntent) {
        when (intent) {
            SearchIntent.Enter -> suggestions()
            is SearchIntent.Edit -> edit(intent.value)
            is SearchIntent.ChooseQuery -> { edit(intent.value, immediate = true); remember() }
            is SearchIntent.RemoveQuery -> history { repository.removeQuery(intent.value) }
            SearchIntent.ClearHistory -> history { repository.clearHistory() }
            SearchIntent.Submit -> { if (!state.value.page.loaded && CatalogSearchRules.valid(CatalogSearchRules.normalize(state.value.query))) { job?.cancel(); generation++; load() }; remember() }
            SearchIntent.Remember -> remember()
            SearchIntent.More -> if (job?.isActive != true && state.value.page.nextCursor != null && !state.value.page.appendFailed) load(append = true)
            SearchIntent.Retry -> { if (state.value.query.isBlank()) suggestions() else load(append = state.value.page.appendFailed && state.value.page.error != CatalogError.Conflict) }
        }
    }
    fun invalidate() { if (state.value.page.loaded) { job?.cancel(); generation++; load() } }
    private fun suggestions() {
        if (state.value.suggestionsLoaded || suggestionJob?.isActive == true) return
        suggestionJob = viewModelScope.launch {
            when (val result = repository.suggestions()) {
                is CatalogResult.Success -> mutable.update { it.copy(suggestions = result.value, suggestionsLoaded = true, suggestionError = null) }
                is CatalogResult.Failure -> { mutable.update { it.copy(suggestionError = result.error) }; expired(result.error) }
            }
        }
    }
    private fun edit(value: String, immediate: Boolean = false) {
        val input = value.take(CatalogSearchRules.MAX_LENGTH)
        if (input == state.value.query && state.value.page.loaded) return
        generation++; job?.cancel()
        mutable.update { it.copy(query = input, page = BookPageState()) }
        if (!CatalogSearchRules.valid(CatalogSearchRules.normalize(input))) return
        val token = generation
        job = viewModelScope.launch { if (!immediate) delay(350); if (generation == token) request(token, append = false) }
    }
    private fun load(append: Boolean = false) {
        if (!CatalogSearchRules.valid(CatalogSearchRules.normalize(state.value.query))) return
        if (job?.isActive == true) job?.cancel()
        val token = ++generation
        job = viewModelScope.launch { request(token, append) }
    }
    private suspend fun request(token: Int, append: Boolean) {
        val query = CatalogSearchRules.normalize(state.value.query)
        val before = state.value.page
        if (append && before.nextCursor == null) return
        mutable.update { it.copy(page = it.page.copy(loading = !it.page.loaded, refreshing = it.page.loaded && !append, loadingMore = append, error = null, appendFailed = false)) }
        val result = repository.books(CatalogQuery(search = query, sort = CatalogSort.Relevance), if (append) before.nextCursor else null)
        if (generation != token) return
        when (result) {
            is CatalogResult.Success -> mutable.update { it.copy(page = BookPageState(items = (if (append) it.page.items + result.value.items else result.value.items).distinctBy { book -> book.id }, loaded = true, nextCursor = result.value.nextCursor, total = result.value.total)) }
            is CatalogResult.Failure -> { mutable.update { it.copy(page = it.page.copy(loading = false, refreshing = false, loadingMore = false, error = result.error, appendFailed = append)) }; expired(result.error) }
        }
    }
    private fun remember() { val query = CatalogSearchRules.normalize(state.value.query); if (CatalogSearchRules.valid(query)) history { repository.rememberQuery(query) } }
    private fun history(action: suspend () -> CatalogResult<Unit>) { viewModelScope.launch { val result = action(); mutable.update { it.copy(historyError = (result as? CatalogResult.Failure)?.error) } } }
    private fun expired(error: CatalogError) { if (error == CatalogError.Unauthorized) events.trySend(CatalogEffect.SessionExpired) }
}
