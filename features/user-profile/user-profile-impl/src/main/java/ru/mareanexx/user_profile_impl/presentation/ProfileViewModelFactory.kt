package ru.mareanexx.user_profile_impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.mareanexx.user_profile_api.domain.repository.ProfileRepository
import ru.mareanexx.user_profile_api.domain.usecase.*
import ru.mareanexx.user_profile_impl.presentation.profile.ProfileViewModel
import ru.mareanexx.user_profile_impl.presentation.achievements.AchievementsViewModel
import ru.mareanexx.user_profile_impl.presentation.settings.SettingsViewModel

class ProfileViewModelFactory(
    private val repository: ProfileRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        ProfileViewModel::class.java -> ProfileViewModel(repository, UpdateProfileUseCase(repository), UpdateAvatarUseCase(repository))
        AchievementsViewModel::class.java -> AchievementsViewModel(repository)
        SettingsViewModel::class.java -> SettingsViewModel(repository)
        else -> error("Неизвестная модель представления профиля")
    } as T
}
