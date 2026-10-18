package ru.mareanexx.catalog_impl.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_api.domain.repository.BookRepository
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_api.domain.repository.CatalogRepository
import ru.mareanexx.catalog_impl.presentation.CatalogEffect
import ru.mareanexx.catalog_impl.presentation.model.*

internal class HomeViewModel(
    private val repository: CatalogRepository,
    private val books: BookRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow(HomeUiState())
    val state = mutable.asStateFlow()
    private val events = Channel<CatalogEffect>(Channel.BUFFERED)
    val effects = events.receiveAsFlow()
    init { viewModelScope.launch { repository.unread.collect { unread -> if (unread != null) mutable.update { it.copy(unreadCount = unread.count) } } } }
    fun accept(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Enter, HomeIntent.Refresh -> load()
            is HomeIntent.Actions -> mutable.update { it.copy(actionBook = intent.book, actionError = null) }
            HomeIntent.CloseActions -> if (!state.value.saving) mutable.update { it.copy(actionBook = null, actionError = null) }
            HomeIntent.ToggleFavorite -> change(true)
            HomeIntent.TogglePlanned -> change(false)
            HomeIntent.Collections -> state.value.actionBook?.takeUnless { state.value.saving }?.let { book ->
                mutable.update { it.copy(actionBook = null, actionError = null) }
                events.trySend(CatalogEffect.OpenBook(book.id, BookEntry.Collections))
            }
            HomeIntent.ClearNotice -> mutable.update { it.copy(notice = null) }
        }
    }
    private fun load() {
        if (state.value.loading || state.value.refreshing) return
        mutable.update { it.copy(loading = it.feed == null, refreshing = it.feed != null, error = null) }
        viewModelScope.launch {
            when (val result = repository.home()) {
                is CatalogResult.Success -> mutable.update { it.copy(feed = result.value, loading = false, refreshing = false) }
                is CatalogResult.Failure -> { mutable.update { it.copy(loading = false, refreshing = false, error = result.error) }; expired(result.error) }
            }
        }
    }
    private fun change(favorite: Boolean) {
        val book = state.value.actionBook ?: return
        if (state.value.saving) return
        val selected = !(if (favorite) book.shelves.favorite else book.shelves.wantToRead)
        mutable.update { it.copy(saving = true, actionError = null) }
        viewModelScope.launch {
            val result = if (favorite) books.setFavorite(book.id, selected) else books.setWantToRead(book.id, selected)
            when (result) {
                is BookResult.Success -> {
                    mutable.update { it.copy(saving = false, actionBook = null, notice = when {
                        favorite && selected -> CatalogNotice.FavoriteAdded
                        favorite -> CatalogNotice.FavoriteRemoved
                        selected -> CatalogNotice.PlannedAdded
                        else -> CatalogNotice.PlannedRemoved
                    }) }
                    load()
                }
                is BookResult.Failure -> {
                    val error = when (result.error) {
                        BookError.Unauthorized -> CatalogError.Unauthorized
                        BookError.Offline -> CatalogError.Offline
                        BookError.Timeout -> CatalogError.Timeout
                        BookError.RateLimited -> CatalogError.RateLimited
                        else -> CatalogError.Unavailable
                    }
                    mutable.update { it.copy(saving = false, actionError = error) }; expired(error)
                }
            }
        }
    }
    private fun expired(error: CatalogError) { if (error == CatalogError.Unauthorized) events.trySend(CatalogEffect.SessionExpired) }
}
