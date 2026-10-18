package ru.mareanexx.book_impl.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.book_api.domain.model.BookRatingSummary
import ru.mareanexx.book_impl.R
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun RatingSummaryCard(value: BookRatingSummary, hasOwnReview: Boolean, onWrite: () -> Unit) {
    val strings = LocalResources.current
    Surface(shape = RoundedCornerShape(22.dp), color = backgroundSecondary) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            if (value.count == 0) {
                Text(strings.getString(R.string.book_no_ratings), style = MaterialTheme.typography.titleSmall, color = titleMain)
                Text(strings.getString(R.string.book_no_reviews_hint), Modifier.padding(top = 6.dp), style = MaterialTheme.typography.labelMedium, color = username)
            } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.widthIn(min = 96.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatBookRating(value.average ?: 0.0), style = MaterialTheme.typography.displayLarge, fontSize = 38.sp, lineHeight = 42.sp, color = titleMain)
                    ReviewStars((value.average ?: 0.0).toFloat(), 16)
                    Text(strings.getString(R.string.book_ratings_count, value.count), Modifier.padding(top = 5.dp), style = MaterialTheme.typography.labelSmall, color = username)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (stars in 5 downTo 1) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stars.toString(), style = MaterialTheme.typography.labelSmall, color = username)
                        LinearProgressIndicator(progress = { value.distribution.getOrElse(stars - 1) { 0 }.toFloat() / value.count }, Modifier.weight(1f).height(5.dp),
                            color = rating, trackColor = ratingTrackColor, gapSize = 0.dp, drawStopIndicator = {})
                    }
                }
            }
            FilledTonalButton(onWrite, Modifier.fillMaxWidth().padding(top = 16.dp).heightIn(min = 44.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = tagVioletBackground.copy(alpha = .65f), contentColor = bodySecondary)) {
                Icon(painterResource(CommonR.drawable.edit_icon), null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(strings.getString(if (hasOwnReview) R.string.book_review_edit else R.string.book_review_write), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
internal fun ReviewStars(value: Float, size: Int = 14) {
    Row {
        repeat(5) { index -> Icon(painterResource(when { value >= index + 1 -> CommonR.drawable.star_filled_icon; value > index -> CommonR.drawable.star_half_icon; else -> CommonR.drawable.star_icon }),
            null, Modifier.size(size.dp), tint = rating) }
    }
}
