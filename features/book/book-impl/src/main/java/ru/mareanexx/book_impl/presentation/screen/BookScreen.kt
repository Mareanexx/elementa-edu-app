package ru.mareanexx.book_impl.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_impl.R
import ru.mareanexx.book_impl.presentation.BookIntent
import ru.mareanexx.book_impl.presentation.components.*
import ru.mareanexx.book_impl.presentation.model.BookUiState
import ru.mareanexx.ui.theme.*
import java.util.Locale
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun BookScreen(state: BookUiState, onIntent: (BookIntent) -> Unit, onCopyIsbn: (String) -> Unit, downloadAction: @Composable () -> Unit = {}) {
    val strings = LocalResources.current
    val book = state.book
    Column(Modifier.fillMaxSize().background(backgroundMain).testTag("book_screen")) {
        BookTopBar(strings.getString(R.string.book_title), { onIntent(BookIntent.Back) }) {
            if (book != null) {
                downloadAction()
                Spacer(Modifier.width(8.dp))
            }
            FilledTonalIconButton({ onIntent(BookIntent.ToggleFavorite) }, Modifier.size(48.dp).testTag("book_favorite"), enabled = book != null && !state.mutationBusy,
                shape = RoundedCornerShape(16.dp), colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = backgroundSecondary, contentColor = if (book?.shelves?.favorite == true) Color(0xFFC26F88) else bodySecondary)) {
                if (state.shelvesBusy) CircularProgressIndicator(Modifier.size(22.dp), color = bodySecondary, strokeWidth = 2.dp)
                else Crossfade(book?.shelves?.favorite == true, animationSpec = tween(if (LocalAppUiPreferences.current.reduceMotion) 0 else 160), label = "book_favorite") { selected ->
                    Icon(painterResource(if (selected) R.drawable.book_heart_filled else R.drawable.book_heart), strings.getString(if (selected) R.string.book_favorite_remove else R.string.book_favorite_add), Modifier.size(25.dp))
                }
            }
        }
        when {
            state.loading && book == null -> BookSkeleton()
            book == null -> Box(Modifier.padding(16.dp)) { state.failure?.let { BookErrorCard(it) { onIntent(BookIntent.Reload) } } }
            else -> LazyColumn(Modifier.fillMaxSize().testTag("book_content"), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { BookHero(book, { onIntent(BookIntent.OpenGallery(0)) }, { onIntent(BookIntent.OpenReviews) }) }
                state.failure?.let { error -> item { BookErrorCard(error) { onIntent(BookIntent.Reload) } } }
                item { BookFacts(book) }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        BookPrimaryButton(strings.getString(when { book.pagesRead == book.pages -> R.string.book_read_again; book.pagesRead > 0 -> R.string.book_continue; else -> R.string.book_read }),
                            { onIntent(BookIntent.Read) }, Modifier.fillMaxWidth().testTag("book_read"), enabled = !state.mutationBusy)
                        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ShelfButton(strings.getString(if (book.shelves.wantToRead) R.string.book_in_reading_list else R.string.book_want_to_read),
                                if (book.shelves.wantToRead) CommonR.drawable.bookmark_added_icon else CommonR.drawable.bookmark_icon, book.shelves.wantToRead,
                                Modifier.weight(1f).fillMaxHeight().testTag("book_plan"), !state.mutationBusy) { onIntent(BookIntent.ToggleWantToRead) }
                            ShelfButton(if (book.shelves.collectionCount == 0) strings.getString(R.string.book_collections) else strings.getString(R.string.book_collections_selected, book.shelves.collectionCount),
                                CommonR.drawable.local_library_icon, book.shelves.collectionCount > 0,
                                Modifier.weight(1f).fillMaxHeight().testTag("book_collections"), !state.mutationBusy) { onIntent(BookIntent.OpenCollections) }
                        }
                    }
                }
                item {
                    Column {
                        BookSectionHeader(strings.getString(R.string.book_description), strings.getString(R.string.book_full)) { onIntent(BookIntent.ShowDescription) }
                        Text(book.description.ifBlank { strings.getString(R.string.book_no_description) }, Modifier.testTag("book_description"),
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp, lineHeight = 21.sp), color = titleMain, maxLines = 6, overflow = TextOverflow.Ellipsis)
                    }
                }
                item { BookEdition(book.metadata, state.metadataExpanded, { onIntent(BookIntent.ToggleMetadata) }, onCopyIsbn) }
                item {
                    Column {
                        BookSectionHeader(strings.getString(R.string.book_rating_reviews), strings.getString(R.string.book_see_all)) { onIntent(BookIntent.OpenReviews) }
                        RatingSummaryCard(book.rating, book.myReview != null) { onIntent(BookIntent.OpenReviewEditor) }
                    }
                }
                book.myReview?.let { own -> item(key = "my_review") { BookReviewCard(own, own.id in state.votingIds, onIntent, preview = true) } }
                book.previewReviews.filter { it.id != book.myReview?.id }.take(1).forEach { review ->
                    item(key = review.id) { BookReviewCard(review, review.id in state.votingIds, onIntent, preview = true) }
                }
            }
        }
    }
}

@Composable
private fun ShelfButton(text: String, icon: Int, selected: Boolean, modifier: Modifier, enabled: Boolean, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = if (selected) tagVioletBackground.copy(alpha = .7f) else backgroundSecondary,
        modifier = modifier.clickable(enabled = enabled, interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)) {
        Row(Modifier.heightIn(min = 52.dp).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(icon), null, Modifier.size(20.dp), tint = bodySecondary)
            Text(text, style = MaterialTheme.typography.labelMedium, color = bodySecondary)
        }
    }
}

@Composable
private fun BookFacts(book: BookDetails) {
    val strings = LocalResources.current
    Surface(shape = RoundedCornerShape(18.dp), color = backgroundSecondary) {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(book.pages.toString() to strings.getString(R.string.book_pages),
                (if (book.fileBytes == 0L) strings.getString(R.string.book_unknown) else strings.getString(R.string.book_size_value, String.format(Locale.forLanguageTag("ru"), "%.1f", book.fileBytes / 1_000_000.0))) to strings.getString(R.string.book_size),
                book.notesCount.toString() to strings.getString(R.string.book_notes)).forEachIndexed { index, (value, label) ->
                if (index != 0) VerticalDivider(Modifier.height(28.dp), color = descriptionBackground.copy(alpha = .5f))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value, style = MaterialTheme.typography.titleSmall, color = titleMain)
                    Text(label, Modifier.padding(top = 3.dp), style = MaterialTheme.typography.labelSmall, color = username)
                }
            }
        }
    }
}

@Composable
private fun BookEdition(meta: BookMetadata, expanded: Boolean, onClick: () -> Unit, onCopy: (String) -> Unit) {
    val strings = LocalResources.current
    Column {
        Row(Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(strings.getString(R.string.book_metadata), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = bodySecondary)
            Icon(painterResource(if (expanded) R.drawable.book_chevron_down else CommonR.drawable.keyboard_arrow_right_icon), null, Modifier.size(20.dp), tint = bodySecondary)
        }
        AnimatedVisibility(expanded, enter = if (LocalAppUiPreferences.current.reduceMotion) EnterTransition.None else fadeIn() + expandVertically(),
            exit = if (LocalAppUiPreferences.current.reduceMotion) ExitTransition.None else fadeOut() + shrinkVertically()) {
            Column(Modifier.fillMaxWidth().padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(strings.getString(R.string.book_publisher) to meta.publisher, strings.getString(R.string.book_year) to meta.year?.toString(),
                    strings.getString(R.string.book_language) to meta.language, strings.getString(R.string.book_format) to meta.format).filter { !it.second.isNullOrBlank() }.forEach { (label, value) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = username)
                        Text(value.orEmpty(), Modifier.weight(1.3f), style = MaterialTheme.typography.labelMedium, color = titleMain)
                    }
                }
                meta.isbn?.takeIf(String::isNotBlank)?.let { isbn -> TextButton({ onCopy(isbn) }, contentPadding = PaddingValues(0.dp)) { Text(strings.getString(R.string.book_isbn) + ": " + isbn, color = bodySecondary) } }
            }
        }
    }
}
