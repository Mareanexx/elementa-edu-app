package ru.mareanexx.user_library_impl.presentation.screen

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_impl.R
import ru.mareanexx.user_library_impl.presentation.LibraryIntent
import ru.mareanexx.user_library_impl.presentation.components.*
import ru.mareanexx.user_library_impl.presentation.model.*
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun LibraryScreen(state: LibraryUiState, onIntent: (LibraryIntent) -> Unit) {
    val strings = LocalResources.current
    val pager = rememberPagerState(initialPage = state.category.ordinal) { LibraryCategory.entries.size }
    val scope = rememberCoroutineScope()
    val preferences = LocalAppUiPreferences.current
    val latestIntent by rememberUpdatedState(onIntent)
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    var toolbarMenu by remember { mutableStateOf(false) }
    fun select(category: LibraryCategory) {
        keyboard?.hide(); focus.clearFocus()
        scope.launch { if (preferences.reduceMotion) pager.scrollToPage(category.ordinal) else pager.animateScrollToPage(category.ordinal, animationSpec = tween(320)) }
    }
    LaunchedEffect(pager) {
        snapshotFlow { pager.targetPage }.distinctUntilChanged().collect { latestIntent(LibraryIntent.SelectCategory(LibraryCategory.entries[it])) }
    }
    Column(Modifier.fillMaxSize().background(backgroundMain).testTag("library_screen")) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(strings.getString(R.string.library_screen_title), Modifier.weight(1f), style = MaterialTheme.typography.headlineLarge, fontSize = 28.sp, color = titleMain)
            Box {
                IconButton({ toolbarMenu = true }, enabled = !state.mutating) { Icon(painterResource(CommonR.drawable.more_vert_icon), strings.getString(R.string.library_menu), tint = titleMain) }
                DropdownMenu(toolbarMenu, { toolbarMenu = false }, shape = RoundedCornerShape(16.dp), containerColor = backgroundSecondary) {
                    DropdownMenuItem(text = { Text(strings.getString(R.string.library_sort)) }, leadingIcon = { Icon(painterResource(R.drawable.library_sort), null) }, onClick = { toolbarMenu = false; onIntent(LibraryIntent.OpenSort) })
                    DropdownMenuItem(text = { Text(strings.getString(R.string.library_refresh)) }, leadingIcon = { Icon(painterResource(R.drawable.library_refresh), null) }, onClick = { toolbarMenu = false; onIntent(LibraryIntent.Refresh) })
                    DropdownMenuItem(text = { Text(strings.getString(R.string.library_help)) }, leadingIcon = { Icon(painterResource(R.drawable.library_info), null) }, onClick = { toolbarMenu = false; onIntent(LibraryIntent.OpenHelp) })
                }
            }
        }
        Text(strings.getString(R.string.library_eyebrow), Modifier.padding(horizontal = 16.dp).padding(top = 2.dp, bottom = 16.dp), style = MaterialTheme.typography.labelMedium, color = username)
        TextField(state.input, { onIntent(LibraryIntent.ChangeSearch(it)) }, Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("library_search")
            .onFocusChanged { onIntent(LibraryIntent.SearchFocus(it.isFocused)) }, enabled = !state.mutating, singleLine = true,
            shape = RoundedCornerShape(16.dp), placeholder = { Text(strings.getString(R.string.library_search_hint), style = MaterialTheme.typography.labelMedium) },
            textStyle = MaterialTheme.typography.labelMedium.copy(color = titleMain, fontFamily = NormalNunito),
            leadingIcon = { Icon(painterResource(R.drawable.library_search), null, Modifier.size(20.dp), tint = username) },
            trailingIcon = { if (state.input.isNotEmpty()) IconButton({ onIntent(LibraryIntent.ClearSearch) }) { Icon(painterResource(CommonR.drawable.close_icon), strings.getString(R.string.library_search_clear), Modifier.size(20.dp), tint = username) } },
            colors = TextFieldDefaults.colors(focusedContainerColor = authFieldBackground, unfocusedContainerColor = authFieldBackground,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent, unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent, cursorColor = bodySecondary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { keyboard?.hide(); focus.clearFocus(); onIntent(LibraryIntent.SubmitSearch) }))
        Spacer(Modifier.height(10.dp))
        LibraryTabs(pager, state.counts.takeIf { state.countsLoaded && !state.searchPending && !state.tooShort }, !state.mutating, ::select)
        when {
            state.tooShort -> Column(Modifier.fillMaxWidth().padding(32.dp)) {
                Text(strings.getString(R.string.library_search_short), style = MaterialTheme.typography.titleSmall, color = titleMain)
                Text(strings.getString(R.string.library_search_short_hint), Modifier.padding(top = 8.dp), style = MaterialTheme.typography.labelMedium, color = username)
            }
            state.searchFocused && state.input.isBlank() && state.recentQueries.isNotEmpty() -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(strings.getString(R.string.library_recent), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, color = titleMain)
                        TextButton({ onIntent(LibraryIntent.ClearHistory) }) { Text(strings.getString(R.string.library_clear_history), color = bodySecondary, fontSize = 12.sp) }
                    }
                }
                items(state.recentQueries) { query -> TextButton({ onIntent(LibraryIntent.ChangeSearch(query)); onIntent(LibraryIntent.SubmitSearch); keyboard?.hide(); focus.clearFocus() }, Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 12.dp)) {
                    Icon(painterResource(R.drawable.library_search), null, Modifier.size(18.dp), tint = username)
                    Text(query, Modifier.weight(1f).padding(start = 12.dp), style = MaterialTheme.typography.labelMedium, color = titleMain)
                } }
                item { Text(strings.getString(R.string.library_history_hint), style = MaterialTheme.typography.labelSmall, color = username) }
            }
            else -> HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth().testTag("library_pager"), userScrollEnabled = !state.mutating, key = { LibraryCategory.entries[it].name }) { index ->
                val category = LibraryCategory.entries[index]
                Box(Modifier.fillMaxSize().then(if (index != pager.currentPage) Modifier.clearAndSetSemantics {} else Modifier)) {
                    if (state.searchPending || !state.ready) LibrarySkeleton()
                    else LibraryPageContent(state.page(category), category, state.searching, state.mutating, onIntent, ::select)
                }
            }
        }
    }
}

@Composable
private fun LibraryPageContent(page: LibraryPageState, category: LibraryCategory, searching: Boolean, busy: Boolean, onIntent: (LibraryIntent) -> Unit, select: (LibraryCategory) -> Unit) {
    val strings = LocalResources.current
    val list = rememberLazyListState()
    val latestIntent by rememberUpdatedState(onIntent)
    LaunchedEffect(list, page.items.size, page.cursor, searching) {
        if (category != LibraryCategory.Completed || searching) snapshotFlow {
            list.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.let { it >= list.layoutInfo.totalItemsCount - 3 } == true
        }.distinctUntilChanged().collect { if (it) latestIntent(LibraryIntent.LoadMore(category)) }
    }
    if (page.loading && page.items.isEmpty()) { LibrarySkeleton(); return }
    LazyColumn(Modifier.fillMaxSize().testTag("library_list_${category.name}"), state = list, contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (searching && page.loaded) item { Text(strings.getString(R.string.library_search_results, page.totalCount), style = MaterialTheme.typography.labelSmall, color = username) }
        if (page.loaded && page.items.isEmpty() && page.failure == null) item { LibraryEmpty(category, searching) { select(LibraryCategory.All) } }
        when {
            searching || category == LibraryCategory.InProgress -> items(page.items, key = { it.id }) { LibraryBookRow(it, onIntent, compact = searching, enabled = !busy) }
            category == LibraryCategory.Completed && page.items.isNotEmpty() -> {
                item {
                    Text(strings.getString(R.string.library_completed_count, page.totalCount), style = MaterialTheme.typography.titleSmall, color = titleMain)
                    Text(strings.getString(R.string.library_completed_subtitle), Modifier.padding(top = 6.dp), style = MaterialTheme.typography.labelMedium, color = username)
                }
                item { BookCarousel(page.items, onIntent, large = true, enabled = !busy, onNearEnd = { onIntent(LibraryIntent.LoadMore(category)) }) }
            }
            category == LibraryCategory.All -> {
                val highlights = page.highlights
                if (!highlights?.inProgress.isNullOrEmpty()) {
                    item { LibrarySection(strings.getString(R.string.library_continue_heading)) { select(LibraryCategory.InProgress) } }
                    items(highlights.inProgress, key = { "continue_${it.id}" }) { LibraryBookRow(it, onIntent, enabled = !busy) }
                }
                if (!highlights?.completed.isNullOrEmpty()) {
                    item { LibrarySection(strings.getString(R.string.library_completed_heading)) { select(LibraryCategory.Completed) } }
                    item { BookCarousel(highlights.completed, onIntent, enabled = !busy) }
                }
                if (!highlights?.wantToRead.isNullOrEmpty()) {
                    item { LibrarySection(strings.getString(R.string.library_plan_heading)) { select(LibraryCategory.WantToRead) } }
                    item { BookCarousel(highlights.wantToRead, onIntent, enabled = !busy) }
                }
                val featured = highlights?.let { h -> (h.inProgress + h.completed + h.wantToRead).map { it.id }.toSet() }.orEmpty()
                val remaining = page.items.filterNot { it.id in featured }
                if (remaining.isNotEmpty()) item { Text(strings.getString(R.string.library_shelf_heading), Modifier.padding(top = 4.dp), style = MaterialTheme.typography.titleSmall, color = titleMain) }
                libraryGrid(remaining, onIntent, !busy)
            }
            else -> libraryGrid(page.items, onIntent, !busy)
        }
        libraryFooter(page, category, onIntent)
    }
}

private fun LazyListScope.libraryGrid(books: List<LibraryBook>, onIntent: (LibraryIntent) -> Unit, enabled: Boolean) {
    items(books.chunked(2), key = { "row_${it.first().id}" }) { row ->
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { LibraryTile(it, onIntent, Modifier.weight(1f).fillMaxHeight(), enabled = enabled) }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

private fun LazyListScope.libraryFooter(page: LibraryPageState, category: LibraryCategory, onIntent: (LibraryIntent) -> Unit) {
    if (page.failure != null && (page.items.isEmpty() || page.failedAppend)) item { LibraryErrorCard(page.failure) { onIntent(LibraryIntent.Retry(category)) } }
    else if (page.loadingMore) item { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp), color = bodySecondary, strokeWidth = 2.dp) } }
    else if (page.cursor != null) item { TextButton({ onIntent(LibraryIntent.LoadMore(category)) }, Modifier.fillMaxWidth()) { Text(LocalResources.current.getString(R.string.library_more), color = bodySecondary) } }
}

@Composable
private fun LibrarySection(title: String, onAll: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, color = titleMain)
        TextButton(onAll, contentPadding = PaddingValues(start = 8.dp)) {
            Text(LocalResources.current.getString(R.string.library_show_all), color = bodySecondary, style = MaterialTheme.typography.labelSmall)
            Icon(painterResource(CommonR.drawable.keyboard_arrow_right_icon), null, Modifier.size(16.dp), tint = bodySecondary)
        }
    }
}

@Composable
private fun BookCarousel(books: List<LibraryBook>, onIntent: (LibraryIntent) -> Unit, large: Boolean = false, enabled: Boolean = true, onNearEnd: () -> Unit = {}) {
    val row = rememberLazyListState()
    val latestNearEnd by rememberUpdatedState(onNearEnd)
    LaunchedEffect(row, books.size) {
        snapshotFlow { row.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.let { it >= books.lastIndex - 1 } == true }
            .distinctUntilChanged().collect { if (it) latestNearEnd() }
    }
    BoxWithConstraints {
        val width = if (large) maxWidth * .83f else maxWidth * .49f
        LazyRow(Modifier.fillMaxWidth().testTag(if (large) "library_completed_carousel" else "library_carousel"), state = row, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(books, key = { it.id }) { LibraryTile(it, onIntent, Modifier.width(width), large, enabled) }
        }
    }
}
