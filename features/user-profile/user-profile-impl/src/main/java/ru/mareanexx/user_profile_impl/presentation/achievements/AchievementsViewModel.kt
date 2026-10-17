package ru.mareanexx.user_profile_impl.presentation.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.mareanexx.user_profile_api.domain.model.ProfileResult
import ru.mareanexx.user_profile_api.domain.repository.ProfileRepository

class AchievementsViewModel(
    private val repository: ProfileRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow(AchievementsUiState())
    val state = mutable.asStateFlow()

    init { load() }

    fun accept(intent: AchievementsIntent) {
        when (intent) {
            AchievementsIntent.Reload -> if (!mutable.value.loading) load()
            is AchievementsIntent.SelectCategory -> mutable.update { it.copy(category = intent.category) }
            is AchievementsIntent.SelectAchievement -> mutable.update { it.copy(selectedId = intent.id) }
            AchievementsIntent.DismissAchievement -> mutable.update { it.copy(selectedId = null) }
        }
    }

    private fun load() {
        mutable.update { it.copy(loading = true, failure = null) }
        viewModelScope.launch {
            when (val result = repository.getAchievements()) {
                is ProfileResult.Success -> mutable.update { it.copy(items = result.value, loading = false) }
                is ProfileResult.Failure -> mutable.update { it.copy(failure = result.reason, loading = false) }
            }
        }
    }
}
