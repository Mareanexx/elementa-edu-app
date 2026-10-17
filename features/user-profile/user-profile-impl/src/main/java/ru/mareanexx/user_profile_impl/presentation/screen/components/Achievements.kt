package ru.mareanexx.user_profile_impl.presentation.screen.components

import ru.mareanexx.user_profile_impl.R
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_impl.presentation.achievements.*
import ru.mareanexx.user_profile_impl.presentation.components.ProfileErrorBanner
import ru.mareanexx.user_profile_impl.presentation.screen.components.skeleton.AchievementStripSkeleton
import ru.mareanexx.common_ui.R as CommonR

@Composable
fun Achievements(state: AchievementsUiState, onIntent: (AchievementsIntent) -> Unit, onShowAll: () -> Unit) {
    val strings = androidx.compose.ui.platform.LocalResources.current
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(strings.getString(R.string.profile_achievements_title), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, color = titleMain)
            TextButton(onClick = onShowAll) {
                Text(strings.getString(R.string.profile_show_all), style = MaterialTheme.typography.labelMedium, color = bodySecondary)
                Icon(painterResource(CommonR.drawable.keyboard_arrow_right_icon), null, Modifier.size(18.dp), tint = bodySecondary)
            }
        }
        when {
            state.loading -> AchievementStripSkeleton()
            state.failure != null -> Box(Modifier.padding(horizontal = 16.dp)) { ProfileErrorBanner(state.failure.error) { onIntent(AchievementsIntent.Reload) } }
            state.items.isEmpty() -> Text(strings.getString(R.string.profile_achievements_empty), Modifier.padding(16.dp), color = username)
            else -> {
                val preview = (state.items.filter { !it.isUnlocked }.take(1) + state.items.filter { it.isUnlocked }.takeLast(3) + state.items)
                    .distinctBy { it.id }.take(4)
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("achievements_preview"), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    preview.forEach { item ->
                        AchievementCard(item, compact = true, modifier = Modifier.weight(1f)) { onIntent(AchievementsIntent.SelectAchievement(item.id)) }
                    }
                }
            }
        }
    }
}
