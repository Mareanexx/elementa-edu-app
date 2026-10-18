package ru.mareanexx.user_library_impl.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_impl.R
import ru.mareanexx.user_library_impl.presentation.LibraryIntent
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import ru.mareanexx.book_api.domain.model.BookEntry
import ru.mareanexx.book_api.domain.model.BookTagKind

@Composable
internal fun LibraryBookRow(book: LibraryBook, onIntent: (LibraryIntent) -> Unit, compact: Boolean = false, enabled: Boolean = true) {
    val strings = LocalResources.current
    val height = if (compact) 134.dp else 168.dp
    Surface(shape = RoundedCornerShape(24.dp), color = backgroundSecondary,
        modifier = Modifier.fillMaxWidth().testTag("library_book_${book.id}").clickable(enabled = enabled, indication = null, interactionSource = remember { MutableInteractionSource() }) { onIntent(LibraryIntent.OpenBook(book)) }) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LibraryCover(book, Modifier.width(if (compact) 88.dp else 110.dp).height(height))
            Column(Modifier.weight(1f).heightIn(min = height)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(book.title, style = MaterialTheme.typography.titleSmall, fontSize = 17.sp, lineHeight = 21.sp, color = titleMain, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(book.authors, Modifier.padding(top = 5.dp), style = MaterialTheme.typography.labelSmall.copy(fontFamily = NormalNunito), fontSize = 12.sp, lineHeight = 16.sp, color = username, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    BookMenuButton(book, onIntent, enabled)
                }
                if (!compact) book.tags.firstOrNull { it.kind == BookTagKind.Topic }?.let { tag ->
                    Text(tag.label, Modifier.padding(top = 8.dp).background(tagVioletBackground.copy(alpha = .6f), RoundedCornerShape(7.dp)).padding(horizontal = 7.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = bodySecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f)) {
                        SmallRating(book)
                        if (book.pagesRead > 0) {
                            LinearProgressIndicator(progress = { book.pagesRead.toFloat() / book.pages }, Modifier.fillMaxWidth().padding(top = 9.dp).height(4.dp), color = bodySecondary,
                                trackColor = authFieldBackground, gapSize = 0.dp, drawStopIndicator = {})
                            Text(strings.getString(R.string.library_progress_caption, book.pagesRead, book.pages), Modifier.padding(top = 6.dp), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = username)
                        } else Text(strings.getString(R.string.library_pages, book.pages), Modifier.padding(top = 5.dp), style = MaterialTheme.typography.labelSmall, color = username)
                    }
                    ReaderArrow(book, onIntent, enabled)
                }
            }
        }
    }
}

@Composable
internal fun LibraryTile(book: LibraryBook, onIntent: (LibraryIntent) -> Unit, modifier: Modifier = Modifier, large: Boolean = false, enabled: Boolean = true) {
    val strings = LocalResources.current
    Surface(shape = RoundedCornerShape(24.dp), color = backgroundSecondary, modifier = modifier.testTag("library_tile_${book.id}")
        .clickable(enabled = enabled, indication = null, interactionSource = remember { MutableInteractionSource() }) { onIntent(LibraryIntent.OpenBook(book)) }) {
        Column(Modifier.padding(12.dp)) {
            Box(Modifier.fillMaxWidth().height(if (large) 220.dp else 168.dp), contentAlignment = Alignment.Center) {
                LibraryCover(book, Modifier.width(if (large) 146.dp else 108.dp).fillMaxHeight())
                Box(Modifier.align(Alignment.TopEnd)) { BookMenuButton(book, onIntent, enabled) }
            }
            Text(book.title, Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleSmall, fontSize = if (large) 20.sp else 16.sp,
                lineHeight = if (large) 25.sp else 20.sp, color = titleMain, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(book.authors, Modifier.padding(top = 4.dp), style = MaterialTheme.typography.labelSmall.copy(fontFamily = NormalNunito), fontSize = 11.sp, color = username, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                SmallRating(book, Modifier.weight(1f))
                Text(strings.getString(R.string.library_pages, book.pages), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = username)
            }
            if (large) FilledTonalButton({ onIntent(LibraryIntent.OpenBook(book, BookEntry.Reader)) }, Modifier.fillMaxWidth().padding(top = 14.dp).heightIn(min = 48.dp), enabled = enabled,
                shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.filledTonalButtonColors(containerColor = tagVioletBackground, contentColor = bodySecondary)) {
                Text(strings.getString(R.string.library_reread), style = MaterialTheme.typography.labelMedium)
            } else Row(Modifier.fillMaxWidth().padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(strings.getString(when {
                    book.status == ReadingStatus.Completed -> R.string.library_completed_badge
                    book.status == ReadingStatus.InProgress -> R.string.library_progress_badge
                    book.shelves.wantToRead -> R.string.library_wanted_badge
                    else -> R.string.library_not_started
                }), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = if (book.status == ReadingStatus.Completed) Color(0xFF5F8F7C) else username, maxLines = 1, overflow = TextOverflow.Ellipsis)
                ReaderArrow(book, onIntent, enabled)
            }
        }
    }
}

@Composable
private fun BookMenuButton(book: LibraryBook, onIntent: (LibraryIntent) -> Unit, enabled: Boolean) {
    IconButton({ onIntent(LibraryIntent.ShowBookMenu(book)) }, Modifier.size(36.dp).testTag("library_menu_${book.id}"), enabled = enabled) {
        Icon(painterResource(CommonR.drawable.more_horiz_icon), LocalResources.current.getString(R.string.library_book_more, book.title), Modifier.size(20.dp), tint = username)
    }
}

@Composable
private fun ReaderArrow(book: LibraryBook, onIntent: (LibraryIntent) -> Unit, enabled: Boolean) {
    FilledTonalIconButton({ onIntent(LibraryIntent.OpenBook(book, BookEntry.Reader)) }, Modifier.size(44.dp).testTag("library_read_${book.id}"), enabled = enabled,
        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = tagVioletBackground.copy(alpha = .7f), contentColor = bodySecondary)) {
        Icon(painterResource(CommonR.drawable.keyboard_arrow_right_icon), LocalResources.current.getString(when (book.status) {
            ReadingStatus.NotStarted -> R.string.library_read
            ReadingStatus.InProgress -> R.string.library_continue
            ReadingStatus.Completed -> R.string.library_reread
        }), Modifier.size(25.dp))
    }
}

@Composable
internal fun SmallRating(book: LibraryBook, modifier: Modifier = Modifier) {
    val strings = LocalResources.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(painterResource(CommonR.drawable.star_filled_icon), null, Modifier.size(13.dp), tint = rating)
        val rating = book.rating
        Text(if (rating == null) strings.getString(R.string.library_no_rating) else ratingText(rating.average),
            Modifier.semantics { contentDescription = if (rating == null) strings.getString(R.string.library_no_rating) else strings.getString(R.string.library_rating, ratingText(rating.average), rating.count) },
            style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, color = if (rating == null) username else titleMain, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
