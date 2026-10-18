package ru.mareanexx.catalog_impl.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.catalog_api.domain.model.CatalogBook
import ru.mareanexx.catalog_impl.R
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR
import java.text.NumberFormat
import java.util.Locale

@Composable
internal fun CatalogBookTile(book: CatalogBook, onOpen: () -> Unit, onMenu: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.testTag("catalog_tile_${book.id}"), color = backgroundSecondary, shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.catalogClick(onClick = onOpen).padding(10.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Box(Modifier.fillMaxWidth().height(172.dp).clip(RoundedCornerShape(16.dp)).background(authFieldBackground.copy(alpha = .5f))) {
                CatalogCover(book, Modifier.align(Alignment.Center).padding(vertical = 10.dp).width(104.dp).fillMaxHeight())
                IconButton(onMenu, Modifier.align(Alignment.TopEnd).size(44.dp).testTag("catalog_menu_${book.id}")) {
                    Icon(painterResource(CommonR.drawable.more_horiz_icon), stringResource(R.string.catalog_book_actions, book.title), tint = titleMain, modifier = Modifier.size(22.dp))
                }
                val status = when {
                    book.pagesRead == book.pages -> R.string.catalog_completed
                    book.pagesRead > 0 -> R.string.catalog_in_progress
                    book.shelves.wantToRead -> R.string.catalog_planned
                    else -> null
                }
                status?.let { Surface(Modifier.align(Alignment.BottomStart).padding(6.dp), color = backgroundSecondary.copy(alpha = .96f), shape = RoundedCornerShape(8.dp)) {
                    Text(stringResource(it), Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = bodySecondary, fontFamily = BoldNunito, fontSize = 10.sp)
                } }
            }
            Text(book.title, Modifier.fillMaxWidth(), color = titleMain, fontFamily = ExtraBoldNunito, fontSize = 16.sp, lineHeight = 20.sp, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(book.authors, color = username, fontFamily = SemiBoldNunito, fontSize = 11.sp, lineHeight = 15.sp, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                CatalogRating(book)
                Text(stringResource(R.string.catalog_pages, book.pages), color = username, fontFamily = BoldNunito, fontSize = 11.sp)
            }
        }
    }
}

@Composable
internal fun CatalogRating(book: CatalogBook) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(painterResource(CommonR.drawable.star_filled_icon), null, Modifier.size(15.dp), tint = rating)
        Text(book.rating?.let { NumberFormat.getNumberInstance(Locale.forLanguageTag("ru")).apply { maximumFractionDigits = 1; minimumFractionDigits = 1 }.format(it.average) } ?: "—", color = titleMain, fontFamily = ExtraBoldNunito, fontSize = 13.sp)
    }
}

@Composable
internal fun CatalogBookRow(book: CatalogBook, onOpen: () -> Unit, onMenu: () -> Unit) {
    Surface(Modifier.fillMaxWidth().testTag("catalog_row_${book.id}"), color = backgroundSecondary, shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.catalogClick(onClick = onOpen).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                CatalogCover(book, Modifier.width(87.dp).height(128.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(book.title, Modifier.weight(1f), color = titleMain, fontFamily = ExtraBoldNunito, fontSize = 17.sp, lineHeight = 22.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        IconButton(onMenu, Modifier.size(32.dp).testTag("catalog_menu_${book.id}")) { Icon(painterResource(CommonR.drawable.more_horiz_icon), stringResource(R.string.catalog_book_actions, book.title), tint = titleMain, modifier = Modifier.size(20.dp)) }
                    }
                    Text(book.authors, color = username, fontFamily = SemiBoldNunito, fontSize = 12.sp, lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) { CatalogRating(book); Text(stringResource(R.string.catalog_pages, book.pages), color = username, fontFamily = BoldNunito, fontSize = 12.sp) }
                    book.tags.firstOrNull()?.let { tag -> Surface(color = tagVioletBackground.copy(alpha = .6f), shape = RoundedCornerShape(8.dp)) {
                        Text(tag.label, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = bodySecondary, fontFamily = BoldNunito, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    } }
                }
            }
            SearchBookBadges(book)
        }
    }
}
