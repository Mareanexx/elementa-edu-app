package ru.mareanexx.user_profile_impl.presentation.screen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.ui.components.content_header.BlockContentHeader
import ru.mareanexx.ui.theme.backgroundSecondary
import ru.mareanexx.ui.theme.titleMain
import ru.mareanexx.user_profile_impl.presentation.model.Achievement
import ru.mareanexx.user_profile_impl.presentation.model.ProfileAchievements
import ru.mareanexx.common_ui.R as CommonUiR

@Composable
fun Achievements(
    achievements: ProfileAchievements,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundSecondary,
                shape = RoundedCornerShape(30.dp),
            ),
    ) {
        BlockContentHeader(
            titleRes = CommonUiR.string.achievements_header,
            buttonText = CommonUiR.string.show_all,
            buttonIcon = CommonUiR.drawable.keyboard_arrow_right_icon,
            onButtonClick = { TODO() },
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(achievements.list) { achievement ->
                Achievement(achievement)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun Achievement(
    achievement: Achievement,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.widthIn(max = 85.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(achievement.image),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.height(86.dp),
        )

        Text(
            text = achievement.title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            color = titleMain,
            fontSize = 14.sp,
            lineHeight = 14.sp,
        )

        Text(
            text = achievement.receiptDate,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = titleMain,
            fontSize = 10.sp,
            lineHeight = 10.sp,
        )
    }
}
