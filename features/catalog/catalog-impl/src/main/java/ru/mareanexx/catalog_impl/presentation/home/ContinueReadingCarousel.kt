package ru.mareanexx.catalog_impl.presentation.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
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
import ru.mareanexx.catalog_api.domain.model.CatalogBook
import ru.mareanexx.catalog_impl.R
import ru.mareanexx.catalog_impl.presentation.components.*
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun ContinueReadingCarousel(books: List<CatalogBook>, onBook: (String) -> Unit, onRead: (String) -> Unit) {
    val visible = books.distinctBy { it.id }.take(5)
    if (visible.isEmpty()) return
    val pager = rememberPagerState { visible.size }
    val reduce = LocalAppUiPreferences.current.reduceMotion
    val position = stringResource(R.string.home_continue_accessibility, pager.settledPage + 1, visible.size)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.padding(start = 16.dp).testTag("home_continue")) {
            HorizontalPager(
                state = pager,
                modifier = Modifier.fillMaxWidth().testTag("home_continue_pager").semantics { stateDescription = position },
                contentPadding = PaddingValues(end = if (visible.size > 1) 32.dp else 16.dp),
                pageSpacing = 12.dp,
                beyondViewportPageCount = 1,
                userScrollEnabled = visible.size > 1,
                key = { visible[it].id },
            ) { index ->
                val book = visible[index]
                ContinueReadingHero(book, index + 1, visible.size, { onBook(book.id) }, { onRead(book.id) })
            }
        }
        if (visible.size > 1) Row(
            Modifier.fillMaxWidth().clearAndSetSemantics {},
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
        ) {
            repeat(visible.size) { index ->
                val selected = pager.settledPage == index
                val width by animateDpAsState(if (selected) 18.dp else 5.dp, tween(if (reduce) 0 else 200), label = "home_continue_indicator")
                Box(Modifier.width(width).height(5.dp).background(if (selected) bodySecondary else bodySecondary.copy(alpha = .18f), RoundedCornerShape(3.dp)))
            }
        }
    }
}

@Composable
private fun ContinueReadingHero(book: CatalogBook, position: Int, count: Int, onOpen: () -> Unit, onRead: () -> Unit) {
    Surface(Modifier.fillMaxWidth().testTag("home_continue_${book.id}"), color = bodySecondary, shape = RoundedCornerShape(26.dp)) {
        Box(Modifier.background(Brush.linearGradient(listOf(Color(0xFF7770E6), Color(0xFF5B54D3), Color(0xFF5149BD))))) {
            Canvas(Modifier.matchParentSize()) { drawCircle(Color.White.copy(alpha = .055f), size.width * .7f, androidx.compose.ui.geometry.Offset(size.width * 1.12f, size.height * 1.2f)) }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(painterResource(CommonR.drawable.bookmark_icon), null, Modifier.size(16.dp), tint = Color.White.copy(alpha = .85f))
                    Text(stringResource(R.string.home_continue), Modifier.weight(1f), color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.labelMedium)
                    if (count > 1) Surface(color = Color.White.copy(alpha = .14f), shape = RoundedCornerShape(8.dp)) {
                        Text(stringResource(R.string.home_continue_position, position, count), Modifier.testTag("home_continue_position_${book.id}").padding(horizontal = 7.dp, vertical = 3.dp),
                            color = Color.White, fontFamily = BoldNunito, fontSize = 10.sp, lineHeight = 14.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CatalogCover(book, Modifier.width(100.dp).height(158.dp).catalogClick(onClick = onOpen))
                    Column(Modifier.weight(1f).heightIn(min = 158.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.catalogClick(onClick = onOpen), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(book.title, color = Color.White, fontFamily = ExtraBoldNunito, fontSize = 19.sp, lineHeight = 23.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(book.authors, color = Color.White.copy(alpha = .76f), fontFamily = SemiBoldNunito, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val progress = book.pagesRead.toFloat() / book.pages
                                Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color.White.copy(alpha = .23f)).progressSemantics(progress)) {
                                    Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(Color(0xFFE2DDFF), RoundedCornerShape(5.dp)))
                                }
                                Text(stringResource(R.string.catalog_page_progress, book.pagesRead, book.pages), color = Color.White.copy(alpha = .82f), fontFamily = BoldNunito, fontSize = 10.sp, lineHeight = 14.sp)
                            }
                            FilledIconButton(onRead, Modifier.size(44.dp).testTag("home_read_${book.id}"), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White.copy(alpha = .18f), contentColor = Color.White), shape = CircleShape) {
                                Icon(painterResource(R.drawable.catalog_arrow), stringResource(R.string.catalog_read, book.title), Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
