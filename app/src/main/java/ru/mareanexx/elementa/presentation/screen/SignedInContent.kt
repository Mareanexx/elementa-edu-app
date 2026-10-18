package ru.mareanexx.elementa.presentation.screen

import ru.mareanexx.book_api.domain.repository.BookRepository
import ru.mareanexx.book_api.domain.model.BookEntry
import ru.mareanexx.user_library_api.domain.repository.LibraryRepository
import ru.mareanexx.user_library_impl.presentation.LibraryRoute
import ru.mareanexx.book_impl.presentation.BookRoute
import ru.mareanexx.book_impl.presentation.BookCollectionPickerRoute
import androidx.compose.ui.platform.LocalResources
import kotlinx.coroutines.launch
import ru.mareanexx.book_impl.R as BookR
import androidx.compose.ui.semantics.clearAndSetSemantics
import ru.mareanexx.collections_api.domain.repository.CollectionsRepository
import ru.mareanexx.collections_impl.presentation.CollectionsRoute
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.mareanexx.auth_api.domain.model.AuthUser
import ru.mareanexx.elementa.presentation.session.model.SessionState
import ru.mareanexx.ui.components.navigation_bar.BottomNavBar
import ru.mareanexx.ui.components.navigation_bar.utils.MainNavigationTabs
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_api.domain.model.AppSettings
import ru.mareanexx.user_profile_api.domain.repository.ProfileRepository
import ru.mareanexx.user_profile_impl.presentation.ProfileRoute

@Composable
internal fun SignedInContent(
    state: SessionState.SignedIn,
    createRepository: (AuthUser) -> ProfileRepository,
    createCollectionsRepository: (AuthUser) -> CollectionsRepository,
    createBookRepository: (AuthUser) -> BookRepository,
    createLibraryRepository: (AuthUser) -> LibraryRepository,
    onSessionExpired: () -> Unit,
    onSignOut: () -> Unit,
) {
    val repository = remember(state.user.id) { createRepository(state.user) }
    val collectionsRepository = remember(state.user.id) { createCollectionsRepository(state.user) }
    val bookRepository = remember(state.user.id) { createBookRepository(state.user) }
    val libraryRepository = remember(state.user.id) { createLibraryRepository(state.user) }
    var collectionPickerBookId by rememberSaveable(state.user.id) { mutableStateOf<String?>(null) }
    val pickerSnackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val strings = LocalResources.current
    var bookEntry by rememberSaveable(state.user.id) { mutableStateOf(BookEntry.Details) }
    var bookId by rememberSaveable(state.user.id) { mutableStateOf<String?>(null) }
    var libraryVersion by remember { mutableIntStateOf(0) }
    LaunchedEffect(libraryRepository) { libraryRepository.changes.collect { libraryVersion++ } }
    LaunchedEffect(bookRepository) { bookRepository.libraryChanges.collect { libraryVersion++ } }
    val settings by repository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    var tab by rememberSaveable(state.user.id) { mutableStateOf(MainNavigationTabs.Home) }
    var showBottomBar by rememberSaveable { mutableStateOf(true) }
    var collectionsScreenColor by remember { mutableStateOf(backgroundMain) }
    var profileScreenColor by remember { mutableStateOf(backgroundMain) }
    val density = LocalDensity.current
    val view = LocalView.current
    DisposableEffect(view, settings.keepScreenOn) {
        val previous = view.keepScreenOn
        view.keepScreenOn = settings.keepScreenOn
        onDispose { view.keepScreenOn = previous }
    }
    BackHandler(enabled = tab != MainNavigationTabs.Home && showBottomBar) { tab = MainNavigationTabs.Home }
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * settings.textScale),
        LocalAppUiPreferences provides AppUiPreferences(settings.reduceMotion, settings.hapticFeedback),
    ) {
        val screenColor = if (bookId != null) backgroundMain else when (tab) {
            MainNavigationTabs.Profile -> profileScreenColor
            MainNavigationTabs.Collections -> collectionsScreenColor
            else -> backgroundMain
        }
        Box(Modifier.fillMaxSize().background(screenColor)) {
            Scaffold(
                modifier = Modifier.safeDrawingPadding().then(if (bookId != null) Modifier.clearAndSetSemantics {} else Modifier),
                containerColor = backgroundMain,
                bottomBar = {
                    AnimatedVisibility(
                        visible = bookId == null && (showBottomBar || tab !in listOf(MainNavigationTabs.Profile, MainNavigationTabs.Collections)),
                        enter = expandVertically(tween(if (settings.reduceMotion) 0 else 460), expandFrom = Alignment.Bottom),
                        exit = shrinkVertically(tween(if (settings.reduceMotion) 0 else 460), shrinkTowards = Alignment.Bottom),
                    ) {
                        BottomNavBar(tab) { selected ->
                            tab = selected
                            showBottomBar = true
                        }
                    }
                },
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (tab) {
                        MainNavigationTabs.Home -> MainScreen(state, onSignOut)
                        MainNavigationTabs.Profile -> ProfileRoute(
                            repository, state.user.id, state.signingOut, state.signOutFailed, onSignOut,
                            onSessionExpired = onSessionExpired,
                            onRootScreenChanged = { showBottomBar = it },
                            onScreenColorChanged = { profileScreenColor = it },
                        )
                        MainNavigationTabs.Library -> LibraryRoute(
                            libraryRepository, bookRepository, state.user.id, libraryVersion,
                            onOpenBook = { id, entry ->
                                if (entry == BookEntry.Collections) collectionPickerBookId = id
                                else { bookEntry = entry; bookId = id }
                            },
                            onSessionExpired = onSessionExpired,
                            externalFeedbackVisible = pickerSnackbar.currentSnackbarData != null,
                        )
                        MainNavigationTabs.Collections -> CollectionsRoute(
                            collectionsRepository, state.user.id, onSessionExpired,
                            onRootScreenChanged = { showBottomBar = it },
                            onScreenColorChanged = { collectionsScreenColor = it },
                            onOpenBook = { bookEntry = BookEntry.Details; bookId = it },
                            refreshVersion = libraryVersion,
                        )
                    }
                }
            }
            AnimatedContent(bookId, Modifier.fillMaxSize().safeDrawingPadding().clipToBounds(), transitionSpec = {
                val forward = targetState != null
                val duration = if (settings.reduceMotion) 0 else 360
                ContentTransform(
                    if (forward) slideInHorizontally(tween(duration)) { it } else EnterTransition.None,
                    if (!forward) slideOutHorizontally(tween(duration)) { it } else ExitTransition.None,
                    targetContentZIndex = if (forward) 1f else -1f, sizeTransform = null,
                )
            }, label = "Book entry") { selectedId ->
                if (selectedId != null) Surface(Modifier.fillMaxSize(), color = backgroundMain) {
                    BookRoute(selectedId, state.user.id, bookRepository, onClose = { bookId = null }, onSessionExpired = onSessionExpired, entry = bookEntry)
                }
            }
            collectionPickerBookId?.let { selected ->
                BookCollectionPickerRoute(selected, state.user.id, bookRepository,
                    onDismiss = { collectionPickerBookId = null },
                    onSaved = {
                        collectionPickerBookId = null
                        scope.launch { pickerSnackbar.showSnackbar(strings.getString(BookR.string.book_collections_saved)) }
                    },
                    onSessionExpired = onSessionExpired,
                )
            }
            SnackbarHost(pickerSnackbar, Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp).padding(bottom = 110.dp)) {
                Snackbar(it, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            }
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().windowInsetsBottomHeight(WindowInsets.safeDrawing)
                .background(if (bookId == null && (showBottomBar || tab !in listOf(MainNavigationTabs.Profile, MainNavigationTabs.Collections))) navBackground else screenColor))
        }
    }
}
