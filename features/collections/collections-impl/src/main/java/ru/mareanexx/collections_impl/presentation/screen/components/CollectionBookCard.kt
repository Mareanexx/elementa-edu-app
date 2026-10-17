package ru.mareanexx.collections_impl.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.mareanexx.collections_api.domain.model.CollectionBook
import ru.mareanexx.collections_impl.R
import ru.mareanexx.collections_impl.presentation.components.*
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun CollectionBookCard(book: CollectionBook, enabled: Boolean, onOpenBook: () -> Unit, onRemove: () -> Unit, onSwipeRemove: () -> Unit) {
    val strings = LocalResources.current
    var menu by remember(book.id) { mutableStateOf(false) }
    val swipe = rememberSwipeToDismissBoxState(positionalThreshold = { it * .55f })
    val scope = rememberCoroutineScope()
    val latestRemove by rememberUpdatedState(onSwipeRemove)
    Column(Modifier.fillMaxWidth().testTag("collection_book_${book.id}").padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SwipeToDismissBox(state = swipe, enableDismissFromStartToEnd = false, gesturesEnabled = enabled,
            modifier = Modifier.clip(RoundedCornerShape(16.dp)).testTag("collection_book_swipe_${book.id}"),
            onDismiss = {
                scope.launch {
                    swipe.snapTo(SwipeToDismissBoxValue.Settled)
                    latestRemove()
                }
            }, backgroundContent = {
                Box(Modifier.fillMaxSize().drawWithContent {
                    val reveal = runCatching { -swipe.requireOffset() }.getOrDefault(0f).coerceAtLeast(0f)
                    clipRect(left = size.width - reveal) { this@drawWithContent.drawContent() }
                }.background(Color(0xFFF4DDE2)), contentAlignment = Alignment.CenterEnd) {
                    Text(strings.getString(R.string.collections_remove_book), Modifier.padding(20.dp), color = Color(0xFFA53E55), style = MaterialTheme.typography.displaySmall)
                }
            }) {
            Row(Modifier.fillMaxWidth().testTag("collection_book_open_${book.id}").clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = enabled, onClick = onOpenBook),
                horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                BookCover(book, Modifier.width(112.dp).height(168.dp).testTag("collection_book_cover_${book.id}"))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Text(book.title, Modifier.weight(1f).padding(top = 4.dp), color = titleMain, style = MaterialTheme.typography.titleSmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Box {
                            IconButton({ menu = true }, enabled = enabled) { Icon(painterResource(CommonR.drawable.more_horiz_icon), strings.getString(R.string.collections_book_more, book.title), tint = titleMain) }
                            DropdownMenu(menu, { menu = false }, shape = RoundedCornerShape(16.dp), containerColor = backgroundSecondary) {
                                DropdownMenuItem(text = { Text(strings.getString(R.string.collections_book_remove)) }, onClick = { menu = false; onRemove() })
                            }
                        }
                    }
                    Text(book.authors, color = username, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    BookRatingRow(book.rating)
                    if (book.pagesRead > 0) Column(Modifier.fillMaxWidth().testTag("collection_book_progress_${book.id}"), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(strings.getString(R.string.collections_reading_pages, book.pagesRead, book.pages), color = username, style = MaterialTheme.typography.labelSmall)
                        LinearProgressIndicator(progress = { (book.pagesRead.toFloat() / book.pages).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color = bodySecondary, trackColor = authFieldBackground)
                    } else Text(strings.getString(R.string.collections_book_pages, book.pages), color = username, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        BookTagsRow(book)
    }
}
