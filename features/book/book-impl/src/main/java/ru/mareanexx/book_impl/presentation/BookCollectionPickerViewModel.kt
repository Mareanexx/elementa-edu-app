package ru.mareanexx.book_impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_api.domain.repository.BookRepository
import ru.mareanexx.book_impl.presentation.model.BookCollectionsState
import ru.mareanexx.book_impl.presentation.model.BookCollectionPickerEffect

/** Выбор подборок доступен поверх любого экрана, без загрузки полного описания книги. */
internal class BookCollectionPickerViewModel(
    private val bookId: String,
    private val repository: BookRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow(BookCollectionsState())
    val state = mutable.asStateFlow()
    private val events = Channel<BookCollectionPickerEffect>(Channel.BUFFERED)
    val effects = events.receiveAsFlow()
    private var request: Job? = null
    init { load() }
    fun accept(intent: BookCollectionPickerIntent) {
        if (mutable.value.saving) return
        when (intent) {
            BookCollectionPickerIntent.Reload -> load()
            BookCollectionPickerIntent.Save -> save()
            BookCollectionPickerIntent.Close -> {
                request?.cancel()
                events.trySend(BookCollectionPickerEffect.Closed)
            }
            is BookCollectionPickerIntent.Toggle -> mutable.update { state ->
                if (state.items.none { it.id == intent.id }) state
                else state.copy(selected = if (intent.id in state.selected) state.selected - intent.id else state.selected + intent.id, failure = null)
            }
        }
    }
    private fun load() {
        request?.cancel()
        mutable.update { it.copy(loading = true, failure = null) }
        request = viewModelScope.launch {
            when (val result = repository.getCollections(bookId)) {
                is BookResult.Success -> mutable.value = BookCollectionsState(
                    items = result.value,
                    selected = result.value.filter(BookCollectionChoice::selected).map(BookCollectionChoice::id).toSet(),
                    loading = false,
                )
                is BookResult.Failure -> mutable.update { it.copy(loading = false, failure = result.error) }
            }
        }
    }
    private fun save() {
        val state = mutable.value
        if (state.loading || state.items.isEmpty()) return
        mutable.update { it.copy(saving = true, failure = null) }
        request = viewModelScope.launch {
            when (val result = repository.saveCollections(bookId, state.selected)) {
                is BookResult.Success -> events.send(BookCollectionPickerEffect.Saved)
                is BookResult.Failure -> mutable.update { it.copy(saving = false, failure = result.error) }
            }
        }
    }
}
