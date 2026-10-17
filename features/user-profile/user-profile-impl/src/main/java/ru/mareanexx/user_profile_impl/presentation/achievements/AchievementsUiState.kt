package ru.mareanexx.user_profile_impl.presentation.achievements

import ru.mareanexx.user_profile_api.domain.model.*

data class AchievementsUiState(
    val items: List<Achievement> = emptyList(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val loaded: Boolean = false,
    val failure: ProfileFailure? = null,
    val category: AchievementCategory? = null,
    val selectedId: String? = null,
) {
    val selected: Achievement? get() = items.firstOrNull { it.id == selectedId }
}
