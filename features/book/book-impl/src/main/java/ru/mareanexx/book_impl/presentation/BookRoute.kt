package ru.mareanexx.book_impl.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import ru.mareanexx.book_api.domain.model.BookEntry
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import ru.mareanexx.book_api.domain.model.BookError
import ru.mareanexx.book_api.domain.repository.BookRepository
import ru.mareanexx.book_impl.R
import ru.mareanexx.book_impl.presentation.components.*
import ru.mareanexx.book_impl.presentation.model.*
import ru.mareanexx.book_impl.presentation.screen.*
import ru.mareanexx.ui.components.feedback.SyncIndicator
import ru.mareanexx.ui.theme.*

@Composable
fun BookRoute(bookId: String, userId: String, repository: BookRepository, onClose: () -> Unit, onSessionExpired: () -> Unit, entry: BookEntry = BookEntry.Details) {
    val model: BookViewModel = viewModel(key = "book_${userId}_${bookId}_${entry.name}", factory = viewModelFactory {
        initializer { BookViewModel(bookId, repository, if (entry == BookEntry.Reader) BookPage.Reader else BookPage.Detail) }
    })
    val state by model.state.collectAsStateWithLifecycle()
    var entryHandled by rememberSaveable(bookId, userId, entry) { mutableStateOf(false) }
    LaunchedEffect(model, entry, state.book?.id) {
        if (!entryHandled && state.book != null) {
            when (entry) {
                BookEntry.Details -> Unit
                BookEntry.Reader -> Unit
                BookEntry.Collections -> model.accept(BookIntent.OpenCollections)
            }
            entryHandled = true
        }
    }
    val strings = LocalResources.current
    val preferences = LocalAppUiPreferences.current
    val snackbar = remember { SnackbarHostState() }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val close by rememberUpdatedState(onClose)
    fun back() {
        if (entry == BookEntry.Reader && model.state.value.page == BookPage.Reader) close()
        else model.accept(BookIntent.Back)
    }
    LaunchedEffect(model) { model.effects.collect { if (it == BookEffect.Close) close() } }
    LaunchedEffect(repository) { model.accept(BookIntent.Reload) }
    LaunchedEffect(state) {
        val live = model.state.value
        if (listOf(live.failure, live.operationError, live.reviews.failure, live.thread?.failure, live.thread?.postError, live.reviewDraft?.requestError, live.collections?.failure).contains(BookError.Unauthorized)) {
            model.accept(BookIntent.SessionExpired)
            onSessionExpired()
        }
    }
    val notice = state.notice?.let { strings.getString(when (it) {
        BookNotice.FavoriteAdded -> R.string.book_favorite_added
        BookNotice.FavoriteRemoved -> R.string.book_favorite_removed
        BookNotice.Planned -> R.string.book_planned_notice
        BookNotice.Unplanned -> R.string.book_unplanned_notice
        BookNotice.CollectionsSaved -> R.string.book_collections_saved
        BookNotice.ReviewSaved -> R.string.book_review_saved
        BookNotice.ReviewDeleted -> R.string.book_review_deleted
        BookNotice.ReplySent -> R.string.book_reply_sent
        BookNotice.ReplyDeleted -> R.string.book_reply_deleted
    }) }
    val errorText = state.operationError?.takeUnless { state.reviewDeleteOpen }?.let { bookErrorText(it) }
    LaunchedEffect(state.noticeVersion, notice, errorText) {
        val message = errorText ?: notice ?: return@LaunchedEffect
        snackbar.currentSnackbarData?.dismiss()
        snackbar.showSnackbar(message)
        model.accept(BookIntent.DismissNotice)
    }
    BackHandler { back() }
    Scaffold(containerColor = backgroundMain, snackbarHost = { SnackbarHost(snackbar) { Snackbar(it, shape = RoundedCornerShape(16.dp)) } }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AnimatedContent(state.page, Modifier.fillMaxSize().clipToBounds(), transitionSpec = {
                val forward = targetState != BookPage.Detail && (initialState == BookPage.Detail || targetState == BookPage.Thread)
                val duration = if (preferences.reduceMotion) 0 else 360
                ContentTransform(slideInHorizontally(tween(duration)) { if (forward) it else -it / 3 }, slideOutHorizontally(tween(duration)) { if (forward) -it / 3 else it },
                    targetContentZIndex = if (forward) 1f else -1f, sizeTransform = null)
            }, label = "Book navigation") { page ->
                val snapshot = remember(page) { mutableStateOf(state) }
                SideEffect { if (state.page == page) snapshot.value = state }
                val shown = if (state.page == page) state else snapshot.value
                when (page) {
                    BookPage.Detail -> BookScreen(shown, model::accept) { isbn -> scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(strings.getString(R.string.book_isbn), isbn))); snackbar.showSnackbar(strings.getString(R.string.book_isbn_copied)) } }
                    BookPage.Reviews -> ReviewsScreen(shown, model::accept)
                    BookPage.Thread -> ReviewThreadScreen(shown, model::accept)
                    BookPage.Reader -> if (shown.book != null) ReaderEntryScreen(shown.book, returnToLibrary = entry == BookEntry.Reader) { back() }
                        else BookScreen(shown, { if (it == BookIntent.Back) back() else model.accept(it) }, {})
                }
            }
            SyncIndicator(visible = (state.refreshing || state.reviews.refreshing) && state.reviewDraft == null && state.collections == null && snackbar.currentSnackbarData == null,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp))
        }
    }
    BookDialogs(state, model::accept)
    state.book?.let { book -> state.galleryIndex?.let { index -> BookGallery(book.images, index, { model.accept(BookIntent.ChangeGalleryPage(it)) }, { model.accept(BookIntent.DismissGallery) }) } }
}
