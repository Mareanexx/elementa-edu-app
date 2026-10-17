package ru.mareanexx.user_profile_impl.presentation.achievements

import ru.mareanexx.user_profile_api.domain.model.AchievementCategory

sealed interface AchievementsIntent {
    data object Reload : AchievementsIntent
    data class SelectCategory(
        val category: AchievementCategory?,
    ) : AchievementsIntent
    data class SelectAchievement(
        val id: String,
    ) : AchievementsIntent
    data object DismissAchievement : AchievementsIntent
}
