package ru.mareanexx.catalog_impl.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import ru.mareanexx.catalog_api.domain.model.CatalogBook
import ru.mareanexx.catalog_impl.R
import ru.mareanexx.catalog_impl.presentation.model.BookPageState
import ru.mareanexx.ui.theme.*

@Composable
internal fun CatalogBookGrid(page: BookPageState, key: String, active: Boolean, onMore: () -> Unit, onRetry: () -> Unit, onBook: (String) -> Unit, onMenu: (CatalogBook) -> Unit) {
    val grid = rememberLazyGridState()
    val currentMore by rememberUpdatedState(onMore)
    LaunchedEffect(grid, active, page.nextCursor, page.loadingMore, page.appendFailed, page.items.size) {
        if (active && page.nextCursor != null && !page.loadingMore && !page.appendFailed) snapshotFlow { grid.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .distinctUntilChanged().collect { if (it >= page.items.size - 4) currentMore() }
    }
    LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxSize().testTag("catalog_grid_$key"), state = grid, contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (page.error != null) item(span = { GridItemSpan(maxLineSpan) }) { CatalogErrorBanner(page.error, onRetry) }
        if (!page.loaded && page.loading) item(span = { GridItemSpan(maxLineSpan) }) { CatalogSkeleton() }
        if (page.loaded && page.items.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) { CatalogEmpty(stringResource(R.string.catalog_empty_title), stringResource(R.string.catalog_empty_body)) }
        items(page.items, key = { it.id }) { book -> CatalogBookTile(book, { onBook(book.id) }, { onMenu(book) }) }
        if (page.loadingMore) item(span = { GridItemSpan(maxLineSpan) }) { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp), color = bodySecondary, strokeWidth = 2.dp) } }
    }
}
