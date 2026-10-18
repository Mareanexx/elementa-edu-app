package ru.mareanexx.catalog_impl.presentation.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_impl.R
import ru.mareanexx.catalog_impl.presentation.components.*
import ru.mareanexx.catalog_impl.presentation.model.HomeUiState
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(state: HomeUiState, intent: (HomeIntent) -> Unit, onSearch: () -> Unit, onNotifications: () -> Unit,
    onLibrary: () -> Unit, onCollections: () -> Unit, onCatalog: (String?, CatalogSort) -> Unit, onBook: (String) -> Unit, onRead: (String) -> Unit) {
    Column(Modifier.fillMaxSize().background(backgroundMain).testTag("home_screen")) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.home_title), Modifier.weight(1f), style = MaterialTheme.typography.headlineLarge, color = titleMain)
            FilledIconButton(onSearch, Modifier.size(42.dp).testTag("home_search"), shape = RoundedCornerShape(15.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = bodySecondary, contentColor = Color.White)) {
                Icon(painterResource(R.drawable.catalog_search), stringResource(R.string.catalog_search), Modifier.size(22.dp))
            }
            IconButton(onNotifications, Modifier.size(44.dp).testTag("home_notifications")) {
                val description = stringResource(R.string.catalog_unread_badge, state.unreadCount)
                BadgedBox(badge = { if (state.unreadCount > 0) Badge(containerColor = Color(0xFFD8839B), contentColor = Color.White) { Text(state.unreadCount.coerceAtMost(99).toString(), fontSize = 9.sp) } }, modifier = Modifier.semantics { stateDescription = description }) {
                    Icon(painterResource(CommonR.drawable.notifications_icon), stringResource(R.string.catalog_notifications), tint = titleMain, modifier = Modifier.size(25.dp))
                }
            }
        }
        PullToRefreshBox(isRefreshing = state.refreshing, onRefresh = { intent(HomeIntent.Refresh) }, modifier = Modifier.weight(1f), indicator = {}) {
            LazyColumn(Modifier.fillMaxSize().testTag("home_feed"), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
                state.error?.let { error -> item { CatalogErrorBanner(error, { intent(HomeIntent.Refresh) }, Modifier.padding(horizontal = 16.dp)) } }
                if (state.feed == null) item { if (state.loading) CatalogSkeleton() }
                state.feed?.let { feed ->
                    item {
                        if (feed.continueReading.isNotEmpty()) ContinueReadingCarousel(feed.continueReading, onBook, onRead)
                        else DiscoveryHero { onCatalog(null, CatalogSort.Recommended) }
                    }
                    item {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            QuickLink(stringResource(R.string.home_library), quantityText(R.plurals.catalog_books, feed.libraryCount), CommonR.drawable.menu_book_icon, Modifier.weight(1f), onLibrary)
                            QuickLink(stringResource(R.string.home_collections), quantityText(R.plurals.catalog_collections, feed.collectionCount), CommonR.drawable.star_icon, Modifier.weight(1f), onCollections)
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            HomeSectionHeader(stringResource(R.string.home_categories)) { onCatalog(null, CatalogSort.Recommended) }
                            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.testTag("home_categories")) {
                                items(feed.categories, key = { it.id }) { category -> Box(Modifier.testTag("home_category_${category.id}")) { CategoryTile(category) { onCatalog(category.id, CatalogSort.Recommended) } } }
                            }
                        }
                    }
                    feed.sections.filter { it.books.isNotEmpty() }.forEach { section ->
                        item(key = section.id) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                HomeSectionHeader(sectionText(section.id)) { onCatalog(null, section.sort) }
                                if (section.id == "recommended") Text(stringResource(R.string.home_recommended_hint), Modifier.padding(horizontal = 16.dp), color = username, style = MaterialTheme.typography.labelMedium)
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.testTag("home_section_${section.id}")) {
                                    items(section.books, key = { it.id }) { book -> CatalogBookTile(book, { onBook(book.id) }, { intent(HomeIntent.Actions(book)) }, Modifier.width(174.dp)) }
                                }
                            }
                        }
                    }
                    if (feed.sections.all { it.books.isEmpty() }) item { CatalogEmpty(stringResource(R.string.catalog_empty_title), stringResource(R.string.catalog_empty_body)) }
                }
            }
        }
    }
}

@Composable
private fun DiscoveryHero(onExplore: () -> Unit) {
    Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), color = bodySecondary, shape = RoundedCornerShape(26.dp)) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.home_start_title), color = Color.White, fontFamily = ExtraBoldNunito, fontSize = 24.sp, lineHeight = 29.sp)
            Text(stringResource(R.string.home_start_body), color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.labelMedium)
            FilledTonalButton(onExplore, colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White, contentColor = bodySecondary)) { Text(stringResource(R.string.home_explore), fontFamily = ExtraBoldNunito) }
        }
    }
}
@Composable
private fun QuickLink(title: String, count: String, icon: Int, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier, color = backgroundSecondary, shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.catalogClick(onClick = onClick).padding(horizontal = 12.dp, vertical = 15.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(icon), null, Modifier.size(25.dp), tint = bodySecondary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Text(title, color = titleMain, fontFamily = ExtraBoldNunito, fontSize = 12.sp, lineHeight = 16.sp)
                Text(count, color = username, fontFamily = SemiBoldNunito, fontSize = 11.sp, lineHeight = 14.sp)
            }
        }
    }
}
@Composable
private fun HomeSectionHeader(title: String, onAll: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), color = titleMain, fontFamily = ExtraBoldNunito, fontSize = 20.sp, lineHeight = 24.sp)
        TextButton(onAll, contentPadding = PaddingValues(horizontal = 4.dp)) { Text(stringResource(R.string.home_all), color = bodySecondary, fontFamily = BoldNunito, fontSize = 12.sp); Icon(painterResource(R.drawable.catalog_arrow), null, Modifier.size(16.dp), tint = bodySecondary) }
    }
}
