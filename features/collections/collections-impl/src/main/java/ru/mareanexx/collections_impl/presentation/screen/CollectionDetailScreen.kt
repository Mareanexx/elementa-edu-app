package ru.mareanexx.collections_impl.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import ru.mareanexx.collections_impl.presentation.screen.components.CollectionBookCard
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ru.mareanexx.collections_impl.R
import ru.mareanexx.collections_impl.presentation.CollectionsIntent
import ru.mareanexx.collections_impl.presentation.components.*
import ru.mareanexx.collections_impl.presentation.model.CollectionsUiState
import ru.mareanexx.collections_impl.presentation.screen.components.skeleton.CollectionsSkeleton
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun CollectionDetailScreen(state: CollectionsUiState, onIntent: (CollectionsIntent) -> Unit) {
    val strings = LocalResources.current
    val item = state.selected
    val reduceMotion = LocalAppUiPreferences.current.reduceMotion
    Column(Modifier.fillMaxSize().background(backgroundSecondary)) {
        CollectionsToolbar(item?.let { collectionTitle(it) } ?: strings.getString(R.string.collections_all_title), { onIntent(CollectionsIntent.Back) }) {
            IconButton({ onIntent(CollectionsIntent.OpenPicker) }, enabled = item != null && !state.booksLoading && state.booksFailure == null) {
                Icon(painterResource(CommonR.drawable.add_icon), strings.getString(R.string.collections_add_books), tint = titleMain)
            }
        }
        if (state.booksLoading) CollectionsSkeleton(books = true)
        else LazyColumn(Modifier.fillMaxSize().testTag("collection_books_list"), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CollectionCoverImage(item?.cover ?: ru.mareanexx.collections_api.domain.model.CollectionCover.Chemistry, item?.image, Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)))
                    Column(Modifier.padding(start = 14.dp)) {
                        Text(bookCount(state.books.size), color = titleMain, style = MaterialTheme.typography.titleSmall)
                        Text(strings.getString(if (item?.isFavorite == true) R.string.collections_detail_favorite_hint else R.string.collections_detail_hint), color = username, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            state.booksFailure?.let { error -> item { CollectionsErrorCard(error) { onIntent(CollectionsIntent.ReloadBooks) } } }
            if (state.books.isEmpty() && state.booksFailure == null) item {
                Column(Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painterResource(CommonR.drawable.menu_book_icon), null, Modifier.size(56.dp), tint = bodySecondary)
                    Text(strings.getString(R.string.collections_detail_empty_title), Modifier.padding(top = 20.dp), color = titleMain, style = MaterialTheme.typography.titleSmall)
                    Text(strings.getString(R.string.collections_detail_empty_body), Modifier.padding(vertical = 12.dp), color = username, style = MaterialTheme.typography.labelMedium)
                    CollectionsPrimaryButton(strings.getString(R.string.collections_add_books), onClick = { onIntent(CollectionsIntent.OpenPicker) })
                }
            }
            itemsIndexed(state.books, key = { _, book -> book.id }) { index, book ->
                Column(if (reduceMotion) Modifier else Modifier.animateItem()) {
                    CollectionBookCard(book, enabled = !state.busy, onOpenBook = { onIntent(CollectionsIntent.OpenBook(book.id)) }, onRemove = { onIntent(CollectionsIntent.RemoveBook(book.id)) }, onSwipeRemove = { onIntent(CollectionsIntent.SwipeRemoveBook(book.id)) })
                    if (index < state.books.lastIndex) HorizontalDivider(Modifier.padding(top = 12.dp), color = titleMain.copy(alpha = .08f))
                }
            }
        }
    }
}
