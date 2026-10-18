package ru.mareanexx.user_library_impl.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ru.mareanexx.book_api.domain.model.BookEntry
import ru.mareanexx.book_api.domain.repository.BookRepository
import ru.mareanexx.user_library_api.domain.model.LibraryError
import ru.mareanexx.user_library_api.domain.repository.LibraryRepository
import ru.mareanexx.user_library_impl.R
import ru.mareanexx.user_library_impl.presentation.components.*
import ru.mareanexx.user_library_impl.presentation.model.LibraryNotice
import ru.mareanexx.user_library_impl.presentation.screen.LibraryScreen
import ru.mareanexx.ui.components.feedback.SyncIndicator
import ru.mareanexx.ui.theme.backgroundMain

@Composable
fun LibraryRoute(
    repository: LibraryRepository,
    bookRepository: BookRepository,
    userId: String,
    refreshVersion: Int,
    onOpenBook: (String, BookEntry) -> Unit,
    onSessionExpired: () -> Unit,
    externalFeedbackVisible: Boolean = false,
) {
    val model: LibraryViewModel = viewModel(key = "library_$userId", factory = viewModelFactory { initializer { LibraryViewModel(repository, bookRepository) } })
    val state by model.state.collectAsStateWithLifecycle()
    var pullRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(state.category, state.page(state.category).refreshing) {
        if (!state.page(state.category).refreshing) pullRefreshing = false
    }
    val strings = LocalResources.current
    val snackbar = remember { SnackbarHostState() }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    fun dispatch(intent: LibraryIntent) {
        if (intent is LibraryIntent.ShowBookMenu) { keyboard?.hide(); focus.clearFocus() }
        model.accept(intent)
    }
    val latestOpenBook by rememberUpdatedState(onOpenBook)
    LaunchedEffect(model, refreshVersion) { if (model.state.value.ready) model.accept(LibraryIntent.Refresh) }
    LaunchedEffect(model) { model.effects.collect { effect -> if (effect is LibraryEffect.OpenBook) {
        keyboard?.hide(); focus.clearFocus(); latestOpenBook(effect.id, effect.entry)
    } } }
    LaunchedEffect(state) {
        val live = model.state.value
        if (live.operationError == LibraryError.Unauthorized || live.sortError == LibraryError.Unauthorized || live.pages.values.any { it.failure == LibraryError.Unauthorized }) {
            model.accept(LibraryIntent.SessionExpired); onSessionExpired()
        }
    }
    val notice = state.notice?.let { strings.getString(when (it) {
        LibraryNotice.FavoriteAdded -> R.string.library_notice_favorite_added
        LibraryNotice.FavoriteRemoved -> R.string.library_notice_favorite_removed
        LibraryNotice.Planned -> R.string.library_notice_planned
        LibraryNotice.Unplanned -> R.string.library_notice_unplanned
        LibraryNotice.Completed -> R.string.library_notice_completed
        LibraryNotice.Reset -> R.string.library_notice_reset
    }) }
    val error = state.operationError?.takeIf { state.pendingStatus == null }?.let { libraryError(it) }
    LaunchedEffect(state.noticeVersion, notice, error) {
        val message = error ?: notice ?: return@LaunchedEffect
        snackbar.currentSnackbarData?.dismiss()
        val result = snackbar.showSnackbar(message, actionLabel = if (error != null) strings.getString(R.string.library_refresh) else null)
        if (result == SnackbarResult.ActionPerformed) model.accept(LibraryIntent.Refresh)
        model.accept(LibraryIntent.DismissNotice)
    }
    BackHandler(enabled = state.input.isNotEmpty() || state.searchFocused) {
        keyboard?.hide(); focus.clearFocus(); model.accept(LibraryIntent.ClearSearch)
    }
    Scaffold(containerColor = backgroundMain, snackbarHost = { SnackbarHost(snackbar) { Snackbar(it, shape = RoundedCornerShape(16.dp)) } }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).imePadding()) {
            LibraryScreen(state, ::dispatch, pullRefreshing) { pullRefreshing = true; dispatch(LibraryIntent.Refresh) }
            SyncIndicator(visible = !pullRefreshing && state.page(state.category).refreshing && !state.searchPending && state.actionBook == null && !state.sortOpen && state.pendingStatus == null && snackbar.currentSnackbarData == null && !externalFeedbackVisible,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp))
        }
    }
    LibraryDialogs(state, ::dispatch)
}
