package ru.mareanexx.catalog_impl.presentation.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.drop
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_impl.R
import ru.mareanexx.catalog_impl.presentation.components.*
import ru.mareanexx.catalog_impl.presentation.model.*
import ru.mareanexx.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogScreen(state: CatalogUiState, intent: (CatalogIntent) -> Unit, onBack: () -> Unit, onSearch: () -> Unit, onBook: (String) -> Unit, onMenu: (CatalogBook) -> Unit) {
    val ids = listOf<String?>(null) + state.categories.map { it.id }
    val selected = ids.indexOf(state.selectedId).coerceAtLeast(0)
    val pager = rememberPagerState(initialPage = selected) { ids.size }
    val reduce = LocalAppUiPreferences.current.reduceMotion
    var sortMenu by remember { mutableStateOf(false) }
    LaunchedEffect(selected, ids) { if (pager.currentPage != selected) { if (reduce) pager.scrollToPage(selected) else pager.animateScrollToPage(selected) } }
    LaunchedEffect(pager, ids) { snapshotFlow { pager.settledPage }.drop(1).collect { index -> if (index < ids.size) intent(CatalogIntent.Select(ids[index])) } }
    Column(Modifier.fillMaxSize()) {
        CatalogTopBar(stringResource(R.string.catalog_title), onBack, onSearch)
        state.categoryError?.let { CatalogErrorBanner(it, { intent(CatalogIntent.Retry) }, Modifier.padding(horizontal = 16.dp)) }
        CatalogTabs(listOf(stringResource(R.string.catalog_all)) + state.categories.map { it.title }, pager.targetPage) { index ->
            intent(CatalogIntent.Select(ids[index]))
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            val category = state.categories.firstOrNull { it.id == state.selectedId }
            Text(if (category != null) category.description else quantityText(R.plurals.catalog_books, state.page.total), Modifier.weight(1f).padding(vertical = 10.dp), color = username, style = MaterialTheme.typography.labelMedium)
            Box {
                TextButton({ sortMenu = true }) { Icon(painterResource(R.drawable.catalog_sort), null, Modifier.size(18.dp), tint = bodySecondary); Spacer(Modifier.width(6.dp)); Text(sortText(state.sort), color = bodySecondary, style = MaterialTheme.typography.labelMedium) }
                DropdownMenu(sortMenu, { sortMenu = false }, containerColor = backgroundSecondary) {
                    CatalogSort.entries.filter { it != CatalogSort.Relevance }.forEach { sort -> DropdownMenuItem(text = { Text(sortText(sort), color = if (state.sort == sort) bodySecondary else titleMain) }, onClick = { sortMenu = false; intent(CatalogIntent.Sort(sort)) }) }
                }
            }
        }
        HorizontalPager(pager, Modifier.weight(1f), key = { ids[it].orEmpty() }) { index ->
            val id = ids[index].orEmpty()
            val page = state.pages[id] ?: BookPageState()
            PullToRefreshBox(isRefreshing = page.refreshing, onRefresh = { intent(CatalogIntent.Refresh) }, indicator = {}) {
                CatalogBookGrid(page, id.ifEmpty { "all" }, pager.settledPage == index, { intent(CatalogIntent.More) }, { intent(CatalogIntent.Retry) }, onBook, onMenu)
            }
        }
    }
}
