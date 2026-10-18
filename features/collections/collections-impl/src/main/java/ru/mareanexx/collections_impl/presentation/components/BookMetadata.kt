package ru.mareanexx.collections_impl.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import ru.mareanexx.collections_api.domain.model.*
import ru.mareanexx.collections_impl.R
import ru.mareanexx.ui.theme.*
import ru.mareanexx.ui.theme.rating as ratingStar
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun BookRatingRow(rating: BookRating?, modifier: Modifier = Modifier) {
    val strings = LocalResources.current
    if (rating == null) {
        Text(strings.getString(R.string.collections_rating_empty), modifier, color = username, style = MaterialTheme.typography.labelSmall)
        return
    }
    val value = NumberFormat.getNumberInstance(Locale.forLanguageTag("ru")).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }.format(rating.average)
    val description = strings.getString(R.string.collections_rating_accessibility, value, rating.count)
    Row(modifier.clearAndSetSemantics { contentDescription = description }, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(painterResource(CommonR.drawable.star_filled_icon), null, Modifier.size(18.dp), tint = ratingStar)
        Text(value, color = titleMain, style = MaterialTheme.typography.displaySmall)
        Text(strings.getString(R.string.collections_rating_count, rating.count), color = username, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
internal fun BookTagsRow(book: CollectionBook, modifier: Modifier = Modifier) {
    if (book.tags.isEmpty()) return
    val strings = LocalResources.current
    val description = strings.getString(R.string.collections_tags_accessibility, book.title)
    Row(modifier.fillMaxWidth().testTag("collection_tags_${book.id}").horizontalScroll(rememberScrollState())
        .semantics { contentDescription = description }, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        book.tags.forEach { tag ->
            val advanced = tag.kind == BookTagKind.Difficulty
            Surface(shape = RoundedCornerShape(8.dp), color = if (advanced) Color(0xFFF0DADB) else Color(0xFFE0DCF9)) {
                Text(tag.label, Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = if (advanced) Color(0xFFB35A64) else bodySecondary,
                    style = MaterialTheme.typography.labelSmall, lineHeight = 14.sp, maxLines = 1)
            }
        }
    }
}
