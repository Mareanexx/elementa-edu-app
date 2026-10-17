package ru.mareanexx.user_profile_impl.presentation.achievements

import ru.mareanexx.user_profile_impl.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_api.domain.model.Achievement
import ru.mareanexx.user_profile_impl.presentation.components.ProfilePrimaryButton

@Composable
internal fun AchievementDialog(achievement: Achievement, onDismiss: () -> Unit) {
    val strings = androidx.compose.ui.platform.LocalResources.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = backgroundSecondary) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(categoryTitle(achievement.category).uppercase(Locale.forLanguageTag("ru")), color = username, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(18.dp))
                AchievementImage(achievement, 126)
                Spacer(Modifier.height(20.dp))
                Text(achievement.title, style = MaterialTheme.typography.displayMedium, color = titleMain, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(achievement.description, style = MaterialTheme.typography.labelLarge, color = username, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Surface(color = if (achievement.isUnlocked) Color(0xFFE8E5FF) else authFieldBackground, shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (achievement.isUnlocked) strings.getString(R.string.profile_achievement_received) else strings.getString(R.string.profile_achievement_locked), color = bodySecondary, style = MaterialTheme.typography.displaySmall)
                        if (achievement.isUnlocked) {
                            Text(LocalDate.parse(achievement.unlockedAt).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"))),
                                color = username, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 6.dp))
                        } else {
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(progress = { achievement.progress }, modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = bodySecondary, trackColor = Color.White)
                            Text(strings.getString(R.string.profile_achievement_progress, achievement.current, achievement.target, achievement.unit), color = titleMain,
                                style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 10.dp))
                            Text(strings.getString(R.string.profile_achievement_remaining, achievement.remaining), color = username, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))
                ProfilePrimaryButton(strings.getString(R.string.profile_understood), loading = false, onClick = onDismiss)
            }
        }
    }
}
