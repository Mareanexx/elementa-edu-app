package ru.mareanexx.user_profile_impl.presentation.achievements

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_impl.R

@Composable
internal fun AchievementCard(item: Achievement, compact: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val strings = androidx.compose.ui.platform.LocalResources.current
    Column(
        modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)).clickable(role = Role.Button, onClickLabel = strings.getString(R.string.profile_achievement_details), onClick = onClick)
            .semantics { stateDescription = if (item.isUnlocked) strings.getString(R.string.profile_achievement_unlocked) else strings.getString(R.string.profile_achievement_accessibility_progress, item.current, item.target) }
            .padding(horizontal = if (compact) 0.dp else 12.dp, vertical = if (compact) 8.dp else 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AchievementImage(item, 100, if (compact) Modifier.fillMaxWidth().aspectRatio(1f) else Modifier)
        Spacer(Modifier.height(8.dp))
        Text(item.title, color = titleMain, textAlign = TextAlign.Center, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.displaySmall.copy(fontSize = if (compact) 12.sp else 14.sp, lineHeight = if (compact) 14.sp else 17.sp))
        Text(if (item.isUnlocked) strings.getString(R.string.profile_achievement_unlocked) else strings.getString(R.string.profile_achievement_progress_short, item.current, item.target),
            color = if (item.isUnlocked) bodySecondary else username, style = MaterialTheme.typography.labelSmall,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
internal fun AchievementImage(item: Achievement, size: Int = 112, modifier: Modifier = Modifier) {
    val drawable = when (item.art) {
        AchievementArt.Red -> R.drawable.red_achievement
        AchievementArt.Blue -> R.drawable.blue_achievement
        AchievementArt.Orange -> R.drawable.orange_achievement
        AchievementArt.Pink -> R.drawable.pink_achievement
    }
    Box(modifier.size(size.dp).clip(AchievementBadgeShape), contentAlignment = Alignment.Center) {
        Image(painterResource(drawable), null, Modifier.fillMaxSize(),
            alpha = if (item.isUnlocked) 1f else 0.48f,
            colorFilter = if (item.isUnlocked) null else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.15f) }))
        if (!item.isUnlocked) Box(Modifier.fillMaxSize().background(Color(0x20727093), RoundedCornerShape(22.dp)))
    }
}

@Composable
internal fun categoryTitle(category: AchievementCategory): String {
    val strings = androidx.compose.ui.platform.LocalResources.current
    return strings.getString(when (category) {
        AchievementCategory.Reading -> R.string.profile_category_reading
        AchievementCategory.Notes -> R.string.profile_category_notes
        AchievementCategory.Tests -> R.string.profile_category_tests
        AchievementCategory.Habits -> R.string.profile_category_habits
    })
}
