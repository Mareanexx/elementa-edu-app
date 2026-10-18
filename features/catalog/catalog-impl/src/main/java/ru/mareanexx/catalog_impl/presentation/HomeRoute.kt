package ru.mareanexx.catalog_impl.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ru.mareanexx.book_api.domain.model.BookEntry
import ru.mareanexx.book_api.domain.repository.BookRepository
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_api.domain.repository.CatalogRepository
import ru.mareanexx.catalog_impl.presentation.catalog.*
import ru.mareanexx.catalog_impl.presentation.components.*
import ru.mareanexx.catalog_impl.presentation.home.*
import ru.mareanexx.catalog_impl.presentation.search.*
import ru.mareanexx.catalog_impl.presentation.notifications.*
import ru.mareanexx.ui.components.feedback.SyncIndicator
import ru.mareanexx.ui.theme.*

@Composable
fun HomeRoute(repository: CatalogRepository, bookRepository: BookRepository, userId: String, refreshVersion: Int,
    onOpenBook: (String, BookEntry) -> Unit, onOpenLibrary: () -> Unit, onOpenCollections: () -> Unit,
    onSessionExpired: () -> Unit, onRootScreenChanged: (Boolean) -> Unit, onScreenColorChanged: (Color) -> Unit) {
    val home: HomeViewModel = viewModel(key = "home_$userId", factory = viewModelFactory { initializer { HomeViewModel(repository, bookRepository) } })
    val catalog: CatalogViewModel = viewModel(key = "catalog_$userId", factory = viewModelFactory { initializer { CatalogViewModel(repository) } })
    val search: SearchViewModel = viewModel(key = "catalog_search_$userId", factory = viewModelFactory { initializer { SearchViewModel(repository) } })
    val notifications: NotificationsViewModel = viewModel(key = "notifications_$userId", factory = viewModelFactory { initializer { NotificationsViewModel(repository) } })
    val homeState by home.state.collectAsStateWithLifecycle()
    val catalogState by catalog.state.collectAsStateWithLifecycle()
    val searchState by search.state.collectAsStateWithLifecycle()
    val notificationState by notifications.state.collectAsStateWithLifecycle()
    var path by rememberSaveable(userId) { mutableStateOf(listOf("home")) }
    val screen = path.last()
    val reduce = LocalAppUiPreferences.current.reduceMotion
    val keyboard = LocalSoftwareKeyboardController.current
    val snackbar = remember { SnackbarHostState() }
    val strings = LocalResources.current
    val goBack = { keyboard?.hide(); if (path.size > 1) path = path.dropLast(1) }
    val openCatalog: (String?, CatalogSort) -> Unit = { id, sort ->
        keyboard?.hide()
        catalog.accept(CatalogIntent.Open(id, sort))
        if (path.last() != "catalog") path = path + "catalog"
    }
    val openSearch = { if (path.last() != "search") path = path + "search" }
    val latestOpen by rememberUpdatedState(onOpenBook)
    val latestExpired by rememberUpdatedState(onSessionExpired)
    val latestCatalog by rememberUpdatedState(openCatalog)
    suspend fun handle(effect: CatalogEffect) {
        when (effect) {
            CatalogEffect.SessionExpired -> latestExpired()
            is CatalogEffect.OpenBook -> latestOpen(effect.id, effect.entry)
            is CatalogEffect.OpenCategory -> latestCatalog(effect.id, CatalogSort.Recommended)
        }
    }
    LaunchedEffect(home) { home.effects.collect { handle(it) } }
    LaunchedEffect(catalog) { catalog.effects.collect { handle(it) } }
    LaunchedEffect(search) { search.effects.collect { handle(it) } }
    LaunchedEffect(notifications) { notifications.effects.collect { handle(it) } }
    LaunchedEffect(screen) {
        onRootScreenChanged(screen == "home"); onScreenColorChanged(backgroundMain)
        when (screen) {
            "home" -> home.accept(HomeIntent.Enter)
            "search" -> search.accept(SearchIntent.Enter)
            "notifications" -> notifications.accept(NotificationsIntent.Enter)
            "catalog" -> if (!catalogState.page.loaded && !catalogState.page.loading) catalog.accept(CatalogIntent.Open(catalogState.selectedId, catalogState.sort))
        }
    }
    var lastRefresh by remember(userId) { mutableIntStateOf(refreshVersion) }
    LaunchedEffect(refreshVersion) {
        if (lastRefresh != refreshVersion) {
            lastRefresh = refreshVersion
            home.accept(HomeIntent.Refresh); catalog.invalidate(); search.invalidate()
        }
    }
    LaunchedEffect(homeState.notice) {
        homeState.notice?.let { snackbar.showSnackbar(strings.getString(noticeResource(it))); home.accept(HomeIntent.ClearNotice) }
    }
    BackHandler(path.size > 1, onBack = goBack)
    CompositionLocalProvider(LocalTextStyle provides androidx.compose.ui.text.TextStyle(fontFamily = fontFamily, fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.sp)) {
        Box(Modifier.fillMaxSize().background(backgroundMain)) {
            AnimatedContent(path, Modifier.fillMaxSize().clipToBounds(), transitionSpec = {
                val forward = targetState.size > initialState.size
                val duration = if (reduce) 0 else 340
                ContentTransform(
                    slideInHorizontally(tween(duration)) { if (forward) it else -it / 4 },
                    slideOutHorizontally(tween(duration)) { if (forward) -it / 4 else it },
                    targetContentZIndex = if (forward) 1f else -1f, sizeTransform = null,
                )
            }, contentKey = { it.last() }, label = "home_navigation") { current ->
                Surface(Modifier.fillMaxSize(), color = backgroundMain) {
                    when (current.last()) {
                        "home" -> HomeScreen(homeState, home::accept, openSearch, { path = path + "notifications" }, onOpenLibrary, onOpenCollections, openCatalog,
                            { onOpenBook(it, BookEntry.Details) }, { onOpenBook(it, BookEntry.Reader) })
                        "catalog" -> CatalogScreen(catalogState, catalog::accept, goBack, openSearch, { onOpenBook(it, BookEntry.Details) }, { home.accept(HomeIntent.Actions(it)) })
                        "search" -> SearchScreen(searchState, search::accept, goBack, { onOpenBook(it, BookEntry.Details) }, { home.accept(HomeIntent.Actions(it)) })
                        "notifications" -> NotificationsScreen(notificationState, notifications::accept, goBack)
                    }
                }
            }
            val refreshing = when (screen) {
                "home" -> homeState.refreshing
                "catalog" -> catalogState.page.refreshing
                "search" -> searchState.page.refreshing
                else -> notificationState.refreshing
            }
            SyncIndicator(refreshing && snackbar.currentSnackbarData == null, Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(16.dp)) { Snackbar(it, shape = RoundedCornerShape(16.dp)) }
            CatalogBookActions(homeState, home::accept)
        }
    }
}
