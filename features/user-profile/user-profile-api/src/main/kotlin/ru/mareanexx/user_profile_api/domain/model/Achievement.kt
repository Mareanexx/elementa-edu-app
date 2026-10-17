package ru.mareanexx.user_profile_api.domain.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val art: AchievementArt,
    val current: Int,
    val target: Int,
    val unit: String,
    val unlockedAt: String?,
) {
    val isUnlocked: Boolean get() = unlockedAt != null
    val progress: Float get() = (current.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
    val remaining: Int get() = (target - current).coerceAtLeast(0)
}
