package ru.mareanexx.collections_impl.presentation

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.mareanexx.ui.components.feedback.SyncIndicator
import ru.mareanexx.collections_impl.presentation.photo.CollectionCoverProcessor
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ru.mareanexx.collections_api.domain.model.CollectionsError
import ru.mareanexx.collections_api.domain.repository.CollectionsRepository
import ru.mareanexx.collections_impl.R
import ru.mareanexx.collections_impl.presentation.components.*
import ru.mareanexx.collections_impl.presentation.model.*
import ru.mareanexx.collections_impl.presentation.screen.*
import ru.mareanexx.ui.theme.*

@Composable
fun CollectionsRoute(
    repository: CollectionsRepository,
    userId: String,
    onSessionExpired: () -> Unit,
    onRootScreenChanged: (Boolean) -> Unit,
    onScreenColorChanged: (Color) -> Unit,
) {
    val model: CollectionsViewModel = viewModel(key = "collections_$userId", factory = viewModelFactory { initializer { CollectionsViewModel(repository) } })
    val state by model.state.collectAsStateWithLifecycle()
    val strings = LocalResources.current
    val preferences = LocalAppUiPreferences.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val processor = remember(context) { CollectionCoverProcessor(context) }
    val photos = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            model.accept(CollectionsIntent.BeginCoverImport)
            scope.launch {
                try {
                    val upload = withContext(Dispatchers.IO) { processor.prepare(uri) }
                    model.accept(CollectionsIntent.UploadCover(upload))
                } catch (cancelled: CancellationException) {
                    model.accept(CollectionsIntent.CoverImportFailed)
                    throw cancelled
                } catch (_: Exception) { model.accept(CollectionsIntent.CoverImportFailed) }
            }
        }
    }
    LaunchedEffect(repository) { model.accept(CollectionsIntent.Reload) }
    LaunchedEffect(state.page) {
        onRootScreenChanged(state.page == CollectionsPage.Overview)
        onScreenColorChanged(if (state.page == CollectionsPage.Overview) backgroundMain else backgroundSecondary)
    }
    LaunchedEffect(state) {
        val live = model.state.value
        if (listOf(live.failure, live.booksFailure, live.editor?.requestError, live.picker?.error, live.operationError).contains(CollectionsError.Unauthorized)) {
            model.accept(CollectionsIntent.SessionExpired)
            onSessionExpired()
        }
    }
    val message = state.notice?.let {
        strings.getString(when (it) {
            CollectionsNotice.BookRestored -> R.string.collections_notice_book_restored
            CollectionsNotice.Created -> R.string.collections_notice_created
            CollectionsNotice.Updated -> R.string.collections_notice_updated
            CollectionsNotice.Deleted -> R.string.collections_notice_deleted
            CollectionsNotice.BooksAdded -> R.string.collections_notice_books_added
            CollectionsNotice.BookRemoved -> R.string.collections_notice_book_removed
            CollectionsNotice.LimitReached -> R.string.collections_error_limit
        })
    }
    val standaloneError = state.operationError?.takeIf { state.deleteId == null && state.removeBookId == null && !state.settingsOpen }?.let { collectionsError(it) }
    LaunchedEffect(state.noticeVersion, message, standaloneError) {
        val text = message ?: standaloneError ?: return@LaunchedEffect
        snackbar.currentSnackbarData?.dismiss()
        val canUndo = state.undoRemoval != null
        val result = snackbar.showSnackbar(text,
            actionLabel = if (canUndo) strings.getString(R.string.collections_undo) else null,
            duration = if (canUndo || standaloneError != null) SnackbarDuration.Long else SnackbarDuration.Short)
        if (result == SnackbarResult.ActionPerformed && canUndo) model.accept(CollectionsIntent.UndoRemoveBook)
        else model.accept(CollectionsIntent.DismissNotice)
    }
    BackHandler(state.page != CollectionsPage.Overview) { if (!state.busy) model.accept(CollectionsIntent.Back) }
    Scaffold(containerColor = Color.Transparent, snackbarHost = { SnackbarHost(snackbar) { Snackbar(it, shape = RoundedCornerShape(16.dp)) } }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
        AnimatedContent(state.page, Modifier.fillMaxSize().clipToBounds(), transitionSpec = {
            val forward = targetState.ordinal > initialState.ordinal
            val enter = when {
                preferences.reduceMotion -> EnterTransition.None
                forward -> slideInHorizontally(tween(360)) { it }
                else -> slideInHorizontally(tween(360)) { -it / 3 }
            }
            val exit = when {
                preferences.reduceMotion -> ExitTransition.None
                forward -> slideOutHorizontally(tween(360)) { -it / 3 }
                else -> slideOutHorizontally(tween(360)) { it }
            }
            ContentTransform(enter, exit, targetContentZIndex = if (forward) 1f else -1f, sizeTransform = null)
        }, label = "Collections navigation") { page ->
            val snapshot = remember(page) { mutableStateOf(state) }
            SideEffect { if (state.page == page) snapshot.value = state }
            val shown = if (state.page == page) state else snapshot.value
            when (page) {
                CollectionsPage.Overview -> CollectionsScreen(shown, false, model::accept)
                CollectionsPage.All -> CollectionsScreen(shown, true, model::accept)
                CollectionsPage.Detail -> CollectionDetailScreen(shown, model::accept)
            }
        }
        SyncIndicator(
            visible = (state.refreshing || state.booksRefreshing) && !state.settingsOpen && !state.sortOpen && snackbar.currentSnackbarData == null,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
        )
        }
    }
    CollectionsDialogs(state, model::accept, onChoosePhoto = {
        try { photos.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        catch (_: ActivityNotFoundException) { model.accept(CollectionsIntent.CoverImportFailed) }
    })
}
