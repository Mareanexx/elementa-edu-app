package ru.mareanexx.collections_impl.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.mareanexx.collections_api.domain.usecase.OrderCollections
import ru.mareanexx.collections_api.domain.validation.CollectionRules
import ru.mareanexx.collections_impl.R
import ru.mareanexx.collections_impl.presentation.CollectionsIntent
import ru.mareanexx.collections_impl.presentation.components.*
import ru.mareanexx.collections_impl.presentation.model.*
import ru.mareanexx.collections_impl.presentation.screen.components.*
import ru.mareanexx.collections_impl.presentation.screen.components.skeleton.CollectionsSkeleton
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun CollectionsScreen(state: CollectionsUiState, all: Boolean, onIntent: (CollectionsIntent) -> Unit) {
    val strings = LocalResources.current
    val visible = OrderCollections.apply(state.items, state.preferences, includeFavorite = all)
    Column(Modifier.fillMaxSize().background(if (all) backgroundSecondary else backgroundMain)) {
        if (!all) CollectionsOverviewHeader(state.loading, { onIntent(CollectionsIntent.Create) }, { onIntent(CollectionsIntent.OpenSettings) })
        else CollectionsToolbar(strings.getString(R.string.collections_all_title), onBack = { onIntent(CollectionsIntent.Back) }) {
            IconButton({ onIntent(CollectionsIntent.Create) }, enabled = !state.loading) {
                Icon(painterResource(CommonR.drawable.add_icon), strings.getString(R.string.collections_add), tint = titleMain)
            }
            IconButton({ onIntent(CollectionsIntent.OpenSort) }) {
                Icon(painterResource(R.drawable.collections_sort_icon), strings.getString(R.string.collections_sort), tint = titleMain)
            }
        }
        state.failure?.let { error ->
            Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                CollectionsErrorCard(error) { onIntent(CollectionsIntent.Reload) }
            }
        }
        if (state.loading) CollectionsSkeleton(overview = !all)
        else LazyVerticalGrid(GridCells.Fixed(2), modifier = Modifier.fillMaxSize().testTag("collections_grid"), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (!all) {
                state.items.firstOrNull { it.isFavorite }?.let { favorite ->
                    item(key = "favorite", span = { GridItemSpan(2) }) { FavoriteCollectionCard(favorite) { onIntent(CollectionsIntent.OpenCollection(favorite.id)) } }
                }
                state.items.firstOrNull { it.isReadingList }?.let { planned ->
                    item(key = "reading-list", span = { GridItemSpan(2) }) { ReadingListCollectionCard(planned) { onIntent(CollectionsIntent.OpenCollection(planned.id)) } }
                }
                item(key = "header", span = { GridItemSpan(2) }) {
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(strings.getString(R.string.collections_your_shelves), Modifier.weight(1f, fill = false), color = titleMain, style = MaterialTheme.typography.titleSmall)
                            Surface(shape = RoundedCornerShape(8.dp), color = backgroundSecondary) {
                                Text(strings.getString(R.string.collections_count_badge, visible.size), Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = username, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        TextButton({ onIntent(CollectionsIntent.OpenAll) }) {
                            Text(strings.getString(R.string.collections_show_all), color = bodySecondary, style = MaterialTheme.typography.labelMedium)
                            Icon(painterResource(CommonR.drawable.keyboard_arrow_right_icon), null, Modifier.size(18.dp), tint = bodySecondary)
                        }
                    }
                }
            } else item(span = { GridItemSpan(2) }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.getString(R.string.collections_slots, state.customCount, 30), color = username, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                    TextButton({ onIntent(CollectionsIntent.OpenSort) }) { Text(sortTitle(state.preferences.sort), color = bodySecondary, style = MaterialTheme.typography.labelSmall) }
                }
            }
            if (visible.none { !it.isFavorite } && state.failure == null) item(span = { GridItemSpan(2) }) {
                Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(strings.getString(if (state.customCount == 0) R.string.collections_empty_title else R.string.collections_hidden_title), color = titleMain, style = MaterialTheme.typography.titleSmall)
                    Text(strings.getString(if (state.customCount == 0) R.string.collections_empty_body else R.string.collections_hidden_body), Modifier.padding(vertical = 12.dp), color = username, style = MaterialTheme.typography.labelMedium)
                    TextButton({ onIntent(if (state.customCount == 0) CollectionsIntent.Create else CollectionsIntent.SetPreferences(state.preferences.copy(showEmpty = true))) }) {
                        Text(strings.getString(if (state.customCount == 0) R.string.collections_add else R.string.collections_show_empty_action), color = bodySecondary)
                    }
                }
            }
            items(visible, key = { it.id }) { CollectionCard(it, onIntent) }
            if (!all && state.customCount in 1 until CollectionRules.MAX_COLLECTIONS) item(key = "create") {
                CreateCollectionCard { onIntent(CollectionsIntent.Create) }
            }
        }
    }
}
