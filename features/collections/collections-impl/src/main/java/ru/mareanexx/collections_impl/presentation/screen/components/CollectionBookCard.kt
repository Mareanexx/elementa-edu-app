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
import androidx.compose.ui.unit.sp
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
    Column(Modifier.fillMaxWidth().testTag("collection_book_${book.id}").padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                Box(Modifier.size(122.dp, 178.dp).background(tagVioletBackground.copy(alpha = .35f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    BookCover(book, Modifier.width(112.dp).height(168.dp).testTag("collection_book_cover_${book.id}"))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(book.title, color = titleMain, style = MaterialTheme.typography.titleSmall.copy(lineHeight = 23.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(book.authors, color = username, style = MaterialTheme.typography.labelMedium.copy(fontFamily = NormalNunito, lineHeight = 17.sp), fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        Box {
                            IconButton({ menu = true }, Modifier.size(32.dp), enabled = enabled) {
                                Icon(painterResource(CommonR.drawable.more_horiz_icon), strings.getString(R.string.collections_book_more, book.title), Modifier.size(22.dp), tint = username)
                            }
                            DropdownMenu(menu, { menu = false }, shape = RoundedCornerShape(16.dp), containerColor = backgroundSecondary) {
                                DropdownMenuItem(text = { Text(strings.getString(R.string.collections_book_remove)) }, onClick = { menu = false; onRemove() })
                            }
                        }
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = tagVioletBackground.copy(alpha = .3f)) {
                        FlowRow(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp), itemVerticalAlignment = Alignment.CenterVertically) {
                            BookRatingRow(book.rating)
                            Text(strings.getString(R.string.collections_book_pages, book.pages), color = username, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (book.description.isNotBlank()) Text(book.description, color = username,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = NormalNunito, lineHeight = 17.sp),
                        maxLines = if (book.pagesRead > 0) 1 else 2, overflow = TextOverflow.Ellipsis)
                    if (book.pagesRead > 0) Column(Modifier.fillMaxWidth().testTag("collection_book_progress_${book.id}"), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(strings.getString(R.string.collections_reading_pages, book.pagesRead, book.pages), color = username, style = MaterialTheme.typography.labelSmall)
                        LinearProgressIndicator(progress = { (book.pagesRead.toFloat() / book.pages).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(5.dp), color = bodySecondary, trackColor = tagVioletBackground, gapSize = 0.dp, drawStopIndicator = {})
                    } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(strings.getString(R.string.collections_book_details), color = bodySecondary, style = MaterialTheme.typography.labelMedium)
                        Icon(painterResource(CommonR.drawable.keyboard_arrow_right_icon), null, Modifier.size(20.dp), tint = bodySecondary)
                    }
                }
            }
        }
        BookTagsRow(book)
    }
}
