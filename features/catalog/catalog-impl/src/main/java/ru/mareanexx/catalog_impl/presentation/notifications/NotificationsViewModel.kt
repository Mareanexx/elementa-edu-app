package ru.mareanexx.catalog_impl.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_api.domain.repository.CatalogRepository
import ru.mareanexx.catalog_impl.presentation.CatalogEffect
import ru.mareanexx.catalog_impl.presentation.model.NotificationUiState

internal class NotificationsViewModel(
    private val repository: CatalogRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow(NotificationUiState())
    val state = mutable.asStateFlow()
    private val events = Channel<CatalogEffect>(Channel.BUFFERED)
    val effects = events.receiveAsFlow()
    private var job: Job? = null
    private var generation = 0
    init { viewModelScope.launch { repository.unread.collect { unread -> if (unread != null) mutable.update { it.copy(unreadCount = unread.count) } } } }
    fun accept(intent: NotificationsIntent) {
        when (intent) {
            NotificationsIntent.Enter, NotificationsIntent.Refresh -> load()
            is NotificationsIntent.Filter -> if (state.value.filter != intent.value && !state.value.saving) {
                cancelLoad(); mutable.update { it.copy(filter = intent.value, items = emptyList(), loaded = false, nextCursor = null, error = null) }; load()
            }
            is NotificationsIntent.Open -> {
                if (intent.value.readAt == null && !state.value.saving) write(intent.value.id, false)
                intent.value.bookId?.let { events.trySend(CatalogEffect.OpenBook(it)) }
                intent.value.categoryId?.let { events.trySend(CatalogEffect.OpenCategory(it)) }
            }
            NotificationsIntent.MarkAll -> if (state.value.loaded && state.value.unreadCount > 0 && !state.value.saving) write(null, true)
            NotificationsIntent.RetryWrite -> if (!state.value.saving && state.value.writeError != null) write(state.value.pendingReadId, state.value.pendingReadAll, state.value.pendingReadThrough)
            NotificationsIntent.More -> if (!state.value.appendFailed && state.value.nextCursor != null) load(append = true)
            NotificationsIntent.Retry -> load(append = state.value.appendFailed && state.value.error != CatalogError.Conflict)
        }
    }
    private fun cancelLoad() { generation++; job?.cancel(); mutable.update { it.copy(loading = false, refreshing = false, loadingMore = false) } }
    private fun load(append: Boolean = false) {
        if (job?.isActive == true || state.value.saving) return
        val before = state.value
        if (append && before.nextCursor == null) return
        val token = ++generation
        mutable.update { it.copy(loading = !it.loaded, refreshing = it.loaded && !append, loadingMore = append, error = null, appendFailed = false) }
        job = viewModelScope.launch {
            val result = repository.notifications(before.filter, if (append) before.nextCursor else null)
            if (generation != token) return@launch
            when (result) {
                is CatalogResult.Success -> mutable.update { it.copy(items = (if (append) it.items + result.value.items else result.value.items).distinctBy { note -> note.id }, loaded = true, loading = false, refreshing = false, loadingMore = false, nextCursor = result.value.nextCursor, snapshotSequence = result.value.snapshotSequence) }
                is CatalogResult.Failure -> { mutable.update { it.copy(loading = false, refreshing = false, loadingMore = false, error = result.error, appendFailed = append) }; expired(result.error) }
            }
        }
    }
    private fun write(id: String?, all: Boolean, through: Long? = null) {
        cancelLoad()
        val sequence = through ?: state.value.snapshotSequence
        mutable.update { it.copy(saving = true, writeError = null, pendingReadId = id, pendingReadAll = all, pendingReadThrough = if (all) sequence else null) }
        viewModelScope.launch {
            val result = if (all) repository.markAllRead(sequence) else repository.markRead(requireNotNull(id))
            when (result) {
                is CatalogResult.Success -> {
                    mutable.update { current -> current.copy(saving = false, pendingReadId = null, pendingReadAll = false, pendingReadThrough = null,
                        items = current.items.map { note -> if ((all && note.sequence <= sequence) || note.id == id) note.copy(readAt = note.readAt ?: result.value.readAt) else note }.filter { current.filter == NotificationFilter.All || it.readAt == null }) }
                    // Перечитываем первую страницу: фильтр непрочитанных мог стать короче.
                    load()
                }
                is CatalogResult.Failure -> { mutable.update { it.copy(saving = false, writeError = result.error) }; expired(result.error) }
            }
        }
    }
    private fun expired(error: CatalogError) { if (error == CatalogError.Unauthorized) events.trySend(CatalogEffect.SessionExpired) }
}
