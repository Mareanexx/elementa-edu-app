package com.example.book_impl.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_impl.presentation.model.BookReviews
import com.example.book_impl.presentation.model.RatingStars
import ru.mareanexx.ui.components.content_header.BlockContentHeader
import ru.mareanexx.ui.theme.divider
import ru.mareanexx.ui.theme.rating
import ru.mareanexx.ui.theme.ratingTrackColor
import ru.mareanexx.ui.theme.titleMain
import ru.mareanexx.common_ui.R as CommonUiR

@Composable
fun BookRatingAndReviews(
    reviews: BookReviews,
    onShowAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        BlockContentHeader(
            titleRes = CommonUiR.string.block_reviews_header,
            buttonText = CommonUiR.string.see_all,
            buttonIcon = CommonUiR.drawable.keyboard_arrow_right_icon,
            onButtonClick = onShowAllClick,
            horizontalPaddings = PaddingValues(0.dp),
        )

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = reviews.rating,
                    style = MaterialTheme.typography.displayLarge,
                    color = titleMain,
                )

                RatingStars(stars = reviews.stars)

                Text(
                    text = reviews.reviewsNumber,
                    style = MaterialTheme.typography.labelMedium,
                    color = titleMain,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))
            VerticalDivider(modifier = Modifier.height(86.dp), color = divider)
            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                StarLinearProgressIndicator(
                    starsNumber = 5,
                    progress = reviews.fiveStarReviewsPercent,
                )
                StarLinearProgressIndicator(
                    starsNumber = 4,
                    progress = reviews.fourStarReviewsPercent,
                )
                StarLinearProgressIndicator(
                    starsNumber = 3,
                    progress = reviews.threeStarReviewsPercent,
                )
                StarLinearProgressIndicator(
                    starsNumber = 2,
                    progress = reviews.twoStarReviewsPercent,
                )
                StarLinearProgressIndicator(
                    starsNumber = 1,
                    progress = reviews.oneStarReviewsPercent,
                )
            }
        }
    }
}

@Composable
private fun StarLinearProgressIndicator(
    starsNumber: Int,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = starsNumber.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontSize = 14.sp, lineHeight = 14.sp,
            color = titleMain,
        )
        LinearProgressIndicator(
            progress = { progress },
            color = rating,
            trackColor = ratingTrackColor,
            strokeCap = StrokeCap.Round,
            modifier = modifier.height(8.dp),
            gapSize = (-20).dp,
            drawStopIndicator = {},
        )
    }
}

@Composable
private fun RatingStars(
    stars: RatingStars,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        repeat(stars.fullyPaintedStars) {
            Icon(
                painter = painterResource(CommonUiR.drawable.star_filled_icon),
                tint = rating,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
        }

        repeat(stars.halfPaintedStars) {
            Icon(
                painter = painterResource(CommonUiR.drawable.star_half_icon),
                tint = rating,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
        }

        repeat(stars.emptyStars) {
            Icon(
                painter = painterResource(CommonUiR.drawable.star_icon),
                tint = rating,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}
