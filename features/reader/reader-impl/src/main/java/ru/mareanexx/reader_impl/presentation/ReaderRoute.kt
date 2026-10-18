package ru.mareanexx.reader_impl.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.core.view.*
import androidx.lifecycle.*
import androidx.lifecycle.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_api.domain.repository.ReaderRepository
import ru.mareanexx.reader_impl.R
import ru.mareanexx.reader_impl.presentation.components.*
import ru.mareanexx.reader_impl.presentation.engine.*
import ru.mareanexx.reader_impl.presentation.model.*
import ru.mareanexx.ui.theme.*

@Composable
fun ReaderRoute(bookId: String, userId: String, repository: ReaderRepository, onClose: () -> Unit) {
    val model: ReaderViewModel = viewModel(key = "reader_${userId}_$bookId", factory = viewModelFactory { initializer { ReaderViewModel(bookId, repository) } })
    val state by model.state.collectAsStateWithLifecycle()
    LaunchedEffect(model) { model.present() }
    val leaseId = remember(bookId, userId) { java.util.UUID.randomUUID().toString() }
    val controller = remember(bookId) { ReaderController() }
    val palette = readerPalette(state.settings.theme)
    val view = LocalView.current
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    var closing by remember { mutableStateOf(false) }
    val close by rememberUpdatedState(onClose)
    DisposableEffect(view, bookId, leaseId) {
        repository.retainBook(bookId, leaseId)
        val window = activity(view.context)?.window
        val bars = window?.let { WindowCompat.getInsetsController(it, view) }
        val statusVisible = ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.statusBars()) ?: true
        val navigationVisible = ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.navigationBars()) ?: true
        val behavior = bars?.systemBarsBehavior
        bars?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        bars?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            if (statusVisible) bars?.show(WindowInsetsCompat.Type.statusBars())
            if (navigationVisible) bars?.show(WindowInsetsCompat.Type.navigationBars())
            behavior?.let { bars?.systemBarsBehavior = it }
            repository.releaseBook(bookId, leaseId)
        }
    }
    DisposableEffect(lifecycle, model) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) model.accept(ReaderIntent.Flush) }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    BackHandler {
        when {
            state.note != null -> model.accept(ReaderIntent.DismissNote)
            state.panel != ReaderPanel.None -> model.accept(ReaderIntent.Panel(ReaderPanel.None))
            state.completion != ReaderCompletion.Hidden -> model.accept(ReaderIntent.DismissCompletion)
            else -> model.accept(ReaderIntent.Panel(ReaderPanel.None))
        }
    }
    val snackbar = remember { SnackbarHostState() }
    val error = state.operationError?.takeIf { state.note == null && state.panel == ReaderPanel.None }?.let { readerErrorText(it) }
    LaunchedEffect(error) { if (error != null) { snackbar.showSnackbar(error); model.accept(ReaderIntent.DismissError) } }
    MaterialTheme(colorScheme = readerColorScheme(state.settings.theme)) {
        Box(Modifier.fillMaxSize().background(palette.paper).testTag("reader_screen")) {
            if (!state.loading && !state.rendering && state.error == null) Spacer(Modifier.size(1.dp).testTag("reader_ready"))
            state.document?.let { document ->
                key(document.book.id, document.version, state.attempt) {
                    val renderedAttempt = state.attempt
                    ReaderWebContent(document, state.settings, state.annotations, controller, state.position, !state.rendering, { if (model.state.value.attempt == renderedAttempt) model.accept(it) }, Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)).testTag("reader_text"))
                }
            }
            if (state.loading || state.rendering || state.error != null) {
                Column(Modifier.fillMaxSize().background(palette.paper).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    if (state.error == null) {
                        val progress = state.transfer?.progress
                        if (progress != null && state.transfer?.phase == TransferPhase.Downloading) CircularProgressIndicator(progress = { progress.toFloat() }, color = palette.accent, strokeWidth = 3.dp)
                        else CircularProgressIndicator(color = palette.accent, strokeWidth = 3.dp)
                        Text(stringResource(if (state.loading) R.string.reader_loading else R.string.reader_rendering), Modifier.padding(top = 24.dp), style = MaterialTheme.typography.titleSmall, color = palette.ink)
                        Text(stringResource(R.string.reader_loading_hint), Modifier.padding(top = 12.dp), color = palette.muted, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge)
                    } else {
                        Text(stringResource(R.string.reader_book_error), style = MaterialTheme.typography.titleSmall, color = palette.ink)
                        Text(readerErrorText(state.error!!), Modifier.padding(vertical = 16.dp), color = palette.muted, textAlign = TextAlign.Center)
                        Button({ model.accept(ReaderIntent.Retry) }) { Text(stringResource(R.string.reader_retry)) }
                    }
                }
            }
            AnimatedVisibility(
                state.chrome || state.loading || state.error != null,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(140)),
            ) {
                ReaderHeader(state, palette, closing) {
                    closing = true
                    scope.launch { controller.flushPosition(); model.finish(); close() }
                }
            }
            AnimatedVisibility(
                state.chrome && !state.loading && state.error == null,
                Modifier.align(Alignment.BottomCenter),
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(140)),
            ) {
                ReaderFooter(state, palette, controller::step) {
                    model.accept(ReaderIntent.Panel(if (state.panel == ReaderPanel.Menu) ReaderPanel.None else ReaderPanel.Menu))
                }
            }
            if (state.panel == ReaderPanel.Notes || state.panel == ReaderPanel.Chapters) ReaderInventory(state, controller, model::accept) { scope.launch { repository.synchronize(bookId) } }
            AnimatedVisibility(
                state.completion != ReaderCompletion.Hidden && state.note == null && state.panel == ReaderPanel.None && !state.rendering,
                Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 79.dp),
                enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 5 },
                exit = fadeOut(tween(140)),
            ) { ReaderCompletionCard(state, model::accept) }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 88.dp, start = 16.dp, end = 16.dp)) { Snackbar(it, shape = RoundedCornerShape(16.dp)) }
        }
        ReaderMenuPopup(state.panel == ReaderPanel.Menu && state.chrome, state.settings.theme, model::accept)
        when (state.panel) {
            ReaderPanel.Settings -> ReaderSettingsPopup(state.settings, model::accept)
            ReaderPanel.Colors, ReaderPanel.Annotation -> ReaderAnnotationPopup(state, model::accept)
            else -> Unit
        }
        if (state.note != null) ReaderNoteSheet(state, model::accept)
    }
}
private fun activity(context: Context): Activity? = when (context) { is Activity -> context; is ContextWrapper -> activity(context.baseContext); else -> null }
