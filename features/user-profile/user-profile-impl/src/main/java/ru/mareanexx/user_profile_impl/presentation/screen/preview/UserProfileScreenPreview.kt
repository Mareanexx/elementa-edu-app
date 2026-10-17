package ru.mareanexx.user_profile_impl.presentation.screen.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.mareanexx.ui.theme.ElementaAppTheme
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_impl.presentation.profile.ProfileUiState
import ru.mareanexx.user_profile_impl.presentation.achievements.AchievementsUiState
import ru.mareanexx.user_profile_impl.presentation.screen.UserProfileScreen

@Preview
@Composable
private fun UserProfileScreenPreview() {
    ElementaAppTheme {
        UserProfileScreen(
            state = ProfileUiState(profile = UserProfile("demo", "Мария Химик", "chemist", "Люблю учить химию через книги", null, ProfileStatistics(24, 41, 8)), loading = false),
            achievements = AchievementsUiState(loading = false),
            onEdit = {}, onSettings = {}, onPhoto = {}, onReload = {}, onAchievementIntent = {}, onShowAll = {},
        )
    }
}
