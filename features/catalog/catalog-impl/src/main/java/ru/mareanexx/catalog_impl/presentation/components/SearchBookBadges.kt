package ru.mareanexx.catalog_impl.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.catalog_api.domain.model.CatalogBook
import ru.mareanexx.catalog_impl.R
import ru.mareanexx.ui.theme.BoldNunito
import ru.mareanexx.ui.theme.bodySecondary
import ru.mareanexx.ui.theme.tagVioletBackground
import ru.mareanexx.common_ui.R as CommonR

/** Принадлежность к подборкам и прогресс показываются независимо друг от друга. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SearchBookBadges(book: CatalogBook) {
    if (book.pagesRead == 0 && !book.shelves.favorite && !book.shelves.wantToRead && book.shelves.collectionCount == 0) return
    FlowRow(
        modifier = Modifier.fillMaxWidth().testTag("catalog_badges_${book.id}"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (book.pagesRead > 0) {
            val completed = book.pagesRead == book.pages
            BookBadge(
                text = if (completed) stringResource(R.string.catalog_completed)
                    else stringResource(R.string.catalog_search_reading, (100L * book.pagesRead / book.pages).toInt()),
                icon = if (completed) R.drawable.catalog_check else CommonR.drawable.menu_book_icon,
                foreground = Color(0xFF407C6A),
                background = Color(0xFFE2F0EA),
                tag = "catalog_badge_${if (completed) "completed" else "reading"}_${book.id}",
            )
        }
        if (book.shelves.favorite) {
            BookBadge(
                text = stringResource(R.string.catalog_search_favorite),
                icon = R.drawable.catalog_heart,
                foreground = Color(0xFFAE5677),
                background = Color(0xFFF6E5EC),
                tag = "catalog_badge_favorite_${book.id}",
            )
        }
        if (book.shelves.wantToRead) {
            BookBadge(
                text = stringResource(R.string.catalog_planned_add),
                icon = CommonR.drawable.bookmark_icon,
                foreground = bodySecondary,
                background = tagVioletBackground.copy(alpha = .7f),
                tag = "catalog_badge_planned_${book.id}",
            )
        }
        if (book.shelves.collectionCount > 0) {
            BookBadge(
                text = stringResource(R.string.catalog_search_collections, book.shelves.collectionCount),
                icon = CommonR.drawable.star_icon,
                foreground = Color(0xFF937134),
                background = Color(0xFFF5ECD9),
                tag = "catalog_badge_collections_${book.id}",
            )
        }
    }
}

@Composable
private fun BookBadge(text: String, icon: Int, foreground: Color, background: Color, tag: String) {
    Surface(color = background, shape = RoundedCornerShape(9.dp), modifier = Modifier.testTag(tag)) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(painterResource(icon), null, Modifier.size(14.dp), tint = foreground)
            Text(text, color = foreground, fontFamily = BoldNunito, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}
