package ru.mareanexx.book_impl.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_impl.R
import ru.mareanexx.book_impl.presentation.BookIntent
import ru.mareanexx.book_impl.presentation.components.*
import ru.mareanexx.book_impl.presentation.model.BookUiState
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun ReviewsScreen(state: BookUiState, onIntent: (BookIntent) -> Unit) {
    val strings = LocalResources.current
    val reviews = state.reviews
    val list = rememberLazyListState()
    val latestIntent by rememberUpdatedState(onIntent)
    var sortOpen by remember { mutableStateOf(false) }
    LaunchedEffect(list, reviews.query, reviews.items.size, reviews.cursor) {
        snapshotFlow { list.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.let { it >= list.layoutInfo.totalItemsCount - 3 } == true }
            .distinctUntilChanged().collect { if (it) latestIntent(BookIntent.LoadMoreReviews) }
    }
    Column(Modifier.fillMaxSize().background(backgroundMain)) {
        BookTopBar(strings.getString(R.string.book_reviews), { onIntent(BookIntent.Back) })
        Text(state.book?.title.orEmpty(), Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp), color = username, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        LazyColumn(Modifier.fillMaxSize().testTag("book_reviews_list"), state = list, contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.book?.let { book -> item { RatingSummaryCard(book.rating, book.myReview != null) { onIntent(BookIntent.OpenReviewEditor) } } }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.getString(R.string.book_review_sort), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = username)
                    Box {
                        TextButton({ sortOpen = true }) {
                            Text(reviewSortTitle(reviews.query.sort), color = bodySecondary, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Icon(painterResource(R.drawable.book_chevron_down), null, Modifier.size(18.dp), tint = bodySecondary)
                        }
                        DropdownMenu(sortOpen, { sortOpen = false }, shape = RoundedCornerShape(16.dp), containerColor = backgroundSecondary) {
                            ReviewSort.entries.forEach { sort -> DropdownMenuItem(text = { Text(reviewSortTitle(sort)) }, onClick = { sortOpen = false; onIntent(BookIntent.ChangeReviewQuery(reviews.query.copy(sort = sort))) }) }
                        }
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf<Int?>(null, 5, 4, 3, 2, 1)) { stars ->
                        val chosen = reviews.query.rating == stars
                        Surface(shape = RoundedCornerShape(12.dp), color = if (chosen) tagVioletBackground else backgroundSecondary,
                            modifier = Modifier.semantics { selected = chosen }.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Tab,
                                onClick = { onIntent(BookIntent.ChangeReviewQuery(reviews.query.copy(rating = stars))) })) {
                            Row(Modifier.heightIn(min = 44.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(stars?.toString() ?: strings.getString(R.string.book_filter_all), color = if (chosen) bodySecondary else username, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                if (stars != null) Icon(painterResource(CommonR.drawable.star_filled_icon), strings.getString(R.string.book_filter_stars, stars), Modifier.size(13.dp), tint = rating)
                            }
                        }
                    }
                }
            }
            if (reviews.loading) item { ReviewSkeleton() }
            reviews.failure?.let { error -> item { BookErrorCard(error) { onIntent(if (reviews.loaded && reviews.cursor != null) BookIntent.LoadMoreReviews else BookIntent.ReloadReviews) } } }
            if (reviews.loaded && reviews.items.isEmpty() && reviews.failure == null) item {
                Text(strings.getString(if (reviews.query.rating == null) R.string.book_no_reviews else R.string.book_filter_empty), Modifier.padding(vertical = 24.dp), color = username, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            items(reviews.items, key = { it.id }) { review -> BookReviewCard(review, review.id in state.votingIds, onIntent) }
            if (reviews.loadingMore) item { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp), color = bodySecondary, strokeWidth = 2.dp) } }
            else if (reviews.cursor != null && reviews.failure == null) item { TextButton({ onIntent(BookIntent.LoadMoreReviews) }, Modifier.fillMaxWidth()) { Text(strings.getString(R.string.book_more_reviews), color = bodySecondary) } }
        }
    }
}

@Composable
private fun reviewSortTitle(value: ReviewSort): String = LocalResources.current.getString(when (value) {
    ReviewSort.Helpful -> R.string.book_sort_helpful
    ReviewSort.Newest -> R.string.book_sort_newest
    ReviewSort.Highest -> R.string.book_sort_highest
    ReviewSort.Lowest -> R.string.book_sort_lowest
})
