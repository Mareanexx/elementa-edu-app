package ru.mareanexx.user_profile_impl.presentation.settings

import ru.mareanexx.user_profile_api.domain.model.AppSettings

sealed interface SettingsIntent {
    data object Reload : SettingsIntent
    data class Update(
        val settings: AppSettings,
    ) : SettingsIntent
    data object ClearImageCache : SettingsIntent
    data object DismissNotice : SettingsIntent
}
