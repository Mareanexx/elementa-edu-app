package ru.mareanexx.book_impl.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_impl.R
import ru.mareanexx.ui.theme.*
import java.util.Locale
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun BookHero(book: BookDetails, onGallery: () -> Unit, onReviews: () -> Unit) {
    val strings = LocalResources.current
    Surface(shape = RoundedCornerShape(24.dp), color = backgroundSecondary) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.width(124.dp).height(184.dp).testTag("book_cover_open")
                    .clickable(enabled = book.images.isNotEmpty(), interactionSource = remember { MutableInteractionSource() }, indication = null,
                        role = Role.Button, onClickLabel = strings.getString(R.string.book_gallery_open), onClick = onGallery)) {
                    BookCover(book.images.firstOrNull()?.url, book.title, Modifier.fillMaxSize())
                    if (book.images.isNotEmpty()) Surface(Modifier.align(Alignment.BottomEnd).padding(6.dp), shape = RoundedCornerShape(8.dp), color = Color(0xCC353269)) {
                        Row(Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(painterResource(R.drawable.book_expand), null, Modifier.size(12.dp), tint = Color.White)
                            Text(strings.getString(R.string.book_gallery_count, 1, book.images.size), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text(book.title, style = MaterialTheme.typography.titleMedium, lineHeight = 25.sp, color = titleMain)
                        Text(book.authors, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.labelMedium, color = username)
                    }
                    Row(Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onReviews),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(painterResource(CommonR.drawable.star_filled_icon), null, Modifier.size(18.dp), tint = rating)
                        val average = book.rating.average
                        if (average != null) {
                            Text(formatBookRating(average), style = MaterialTheme.typography.titleSmall, color = titleMain)
                            Text(strings.getString(R.string.book_ratings_count, book.rating.count), style = MaterialTheme.typography.labelSmall, color = username)
                        } else Text(strings.getString(R.string.book_no_ratings), style = MaterialTheme.typography.labelSmall, color = username)
                    }
                    if (book.tags.isNotEmpty()) FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        book.tags.forEach { tag ->
                            val difficulty = tag.kind == BookTagKind.Difficulty
                            Text(tag.label, Modifier.background(if (difficulty) tagRedBackground.copy(alpha = .75f) else tagVioletBackground.copy(alpha = .7f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, color = if (difficulty) Color(0xFFAA5266) else bodySecondary)
                        }
                    }
                }
            }
            if (book.pagesRead > 0) {
                Text(strings.getString(if (book.pagesRead == book.pages) R.string.book_completed else R.string.book_progress, book.pagesRead, book.pages),
                    Modifier.padding(top = 16.dp, bottom = 6.dp), style = MaterialTheme.typography.labelSmall, color = username)
                LinearProgressIndicator(progress = { book.pagesRead.toFloat() / book.pages }, Modifier.fillMaxWidth().height(4.dp).testTag("book_reading_progress"), color = bodySecondary,
                    trackColor = authFieldBackground, gapSize = 0.dp, drawStopIndicator = {})
            }
        }
    }
}

internal fun formatBookRating(value: Double): String = String.format(Locale.forLanguageTag("ru"), "%.1f", value)
