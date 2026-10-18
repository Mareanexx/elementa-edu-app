package ru.mareanexx.user_library_impl.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.book_api.domain.model.BookEntry
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_impl.R
import ru.mareanexx.user_library_impl.presentation.LibraryIntent
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun LibraryBookRow(book: LibraryBook, onIntent: (LibraryIntent) -> Unit, compact: Boolean = false, enabled: Boolean = true) {
    val strings = LocalResources.current
    val inProgress = book.status == ReadingStatus.InProgress
    val coverHeight = if (compact) 150.dp else 166.dp
    Surface(shape = RoundedCornerShape(22.dp), color = backgroundSecondary,
        modifier = Modifier.fillMaxWidth().testTag("library_book_${book.id}")
            .clickable(enabled = enabled, indication = null, interactionSource = remember { MutableInteractionSource() }) { onIntent(LibraryIntent.OpenBook(book)) }) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LibraryCover(book, Modifier.width(if (compact) 94.dp else 106.dp).height(coverHeight))
            Column(Modifier.weight(1f).then(if (inProgress) Modifier.heightIn(min = coverHeight) else Modifier),
                verticalArrangement = if (inProgress) Arrangement.SpaceBetween else Arrangement.spacedBy(9.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(book.title, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, lineHeight = 22.sp, color = titleMain, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        BookMenuButton(book, onIntent, enabled)
                    }
                    Text(book.authors, style = MaterialTheme.typography.labelMedium.copy(fontFamily = NormalNunito, lineHeight = 17.sp), fontSize = 13.sp, color = username, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SmallRating(book)
                        Text(strings.getString(R.string.library_pages, book.pages), style = MaterialTheme.typography.labelSmall, color = username)
                    }
                    if (!inProgress) {
                        ReadingBadge(book)
                        ReaderAction(book, onIntent, enabled, Modifier.fillMaxWidth())
                    }
                }
                if (inProgress) Box(Modifier.padding(top = 12.dp)) { ReadingFooter(book, onIntent, enabled) }
            }
        }
    }
}

@Composable
private fun ReadingFooter(book: LibraryBook, onIntent: (LibraryIntent) -> Unit, enabled: Boolean) {
    val strings = LocalResources.current
    val progress = (book.pagesRead.toFloat() / book.pages).coerceIn(0f, 1f)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth().height(12.dp).progressSemantics(progress)
                .background(tagVioletBackground, RoundedCornerShape(6.dp))) {
                Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(bodySecondary, RoundedCornerShape(6.dp)))
            }
            Text(strings.getString(R.string.library_progress_caption, book.pagesRead, book.pages),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, lineHeight = 18.sp), color = username)
        }
        FilledTonalIconButton({ onIntent(LibraryIntent.OpenBook(book, BookEntry.Reader)) }, Modifier.size(48.dp).testTag("library_read_${book.id}"),
            enabled = enabled, shape = CircleShape,
            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = tagVioletBackground, contentColor = bodySecondary)) {
            Icon(painterResource(R.drawable.library_read_chevron), strings.getString(R.string.library_continue), Modifier.size(32.dp))
        }
    }
}

@Composable
internal fun LibraryTile(book: LibraryBook, onIntent: (LibraryIntent) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val strings = LocalResources.current
    Surface(shape = RoundedCornerShape(22.dp), color = backgroundSecondary, modifier = modifier.testTag("library_tile_${book.id}")
        .clickable(enabled = enabled, indication = null, interactionSource = remember { MutableInteractionSource() }) { onIntent(LibraryIntent.OpenBook(book)) }) {
        Column(Modifier.padding(10.dp)) {
            Box(Modifier.fillMaxWidth().height(170.dp).background(
                Brush.verticalGradient(listOf(Color(0xFFECE9F6), Color(0xFFF4F2FA))), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                LibraryCover(book, Modifier.width(102.dp).height(150.dp))
                Box(Modifier.align(Alignment.TopEnd).padding(2.dp)) { BookMenuButton(book, onIntent, enabled) }
                if (book.status == ReadingStatus.InProgress) {
                    Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(6.dp), shape = RoundedCornerShape(10.dp), color = backgroundSecondary.copy(alpha = .96f)) {
                        Box(Modifier.padding(8.dp)) { ReadingProgress(book) }
                    }
                } else Box(Modifier.align(Alignment.BottomStart).padding(6.dp)) { ReadingBadge(book) }
            }
            Text(book.title, Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleSmall, fontSize = 16.sp,
                lineHeight = 21.sp, color = titleMain, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(book.authors, Modifier.padding(top = 5.dp), style = MaterialTheme.typography.labelSmall.copy(fontFamily = NormalNunito),
                fontSize = 12.sp, lineHeight = 16.sp, minLines = 2, maxLines = 2, color = username, overflow = TextOverflow.Ellipsis)
            Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                SmallRating(book, Modifier.weight(1f))
                Text(strings.getString(R.string.library_pages, book.pages), style = MaterialTheme.typography.labelSmall, color = username)
            }
            ReaderAction(book, onIntent, enabled, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ReadingBadge(book: LibraryBook) {
    val completed = book.status == ReadingStatus.Completed
    val planned = book.shelves.wantToRead
    val text = when {
        completed -> R.string.library_completed_badge
        planned -> R.string.library_wanted_badge
        else -> R.string.library_not_started
    }
    Surface(shape = RoundedCornerShape(8.dp), color = when {
        completed -> Color(0xFFE5F1EB)
        planned -> Color(0xFFECE8FC)
        else -> Color(0xFFF0EEF6)
    }) {
        Text(LocalResources.current.getString(text), Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall, fontSize = 12.sp, lineHeight = 16.sp,
            color = if (completed) Color(0xFF467C65) else if (planned) bodySecondary else username, maxLines = 2)
    }
}

@Composable
private fun ReadingProgress(book: LibraryBook) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(LocalResources.current.getString(R.string.library_progress_caption, book.pagesRead, book.pages), style = MaterialTheme.typography.labelSmall, fontSize = 12.sp, color = username)
        LinearProgressIndicator(progress = { (book.pagesRead.toFloat() / book.pages).coerceIn(0f, 1f) }, Modifier.fillMaxWidth().height(5.dp),
            color = bodySecondary, trackColor = tagVioletBackground, gapSize = 0.dp, drawStopIndicator = {})
    }
}

@Composable
private fun BookMenuButton(book: LibraryBook, onIntent: (LibraryIntent) -> Unit, enabled: Boolean) {
    IconButton({ onIntent(LibraryIntent.ShowBookMenu(book)) }, Modifier.size(32.dp).testTag("library_menu_${book.id}"), enabled = enabled) {
        Icon(painterResource(CommonR.drawable.more_horiz_icon), LocalResources.current.getString(R.string.library_book_more, book.title), Modifier.size(22.dp), tint = username)
    }
}

@Composable
private fun ReaderAction(book: LibraryBook, onIntent: (LibraryIntent) -> Unit, enabled: Boolean, modifier: Modifier) {
    val label = LocalResources.current.getString(when (book.status) {
        ReadingStatus.NotStarted -> R.string.library_read
        ReadingStatus.InProgress -> R.string.library_continue_short
        ReadingStatus.Completed -> R.string.library_reread
    })
    FilledTonalButton({ onIntent(LibraryIntent.OpenBook(book, BookEntry.Reader)) }, modifier.heightIn(min = 44.dp).testTag("library_read_${book.id}"),
        enabled = enabled, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        colors = ButtonDefaults.filledTonalButtonColors(containerColor = tagVioletBackground.copy(alpha = .75f), contentColor = bodySecondary)) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.displaySmall, fontSize = 13.sp, lineHeight = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Icon(painterResource(R.drawable.library_arrow_right), null, Modifier.size(20.dp))
    }
}

@Composable
internal fun SmallRating(book: LibraryBook, modifier: Modifier = Modifier) {
    val strings = LocalResources.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(painterResource(CommonR.drawable.star_filled_icon), null, Modifier.size(16.dp), tint = rating)
        val rating = book.rating
        Text(if (rating == null) strings.getString(R.string.library_no_rating) else ratingText(rating.average),
            Modifier.semantics { contentDescription = if (rating == null) strings.getString(R.string.library_no_rating) else strings.getString(R.string.library_rating, ratingText(rating.average), rating.count) },
            style = MaterialTheme.typography.displaySmall, fontSize = 13.sp, color = if (rating == null) username else titleMain, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
