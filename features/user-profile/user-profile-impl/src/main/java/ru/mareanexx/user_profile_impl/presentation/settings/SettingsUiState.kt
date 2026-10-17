package ru.mareanexx.user_profile_impl.presentation.settings

import ru.mareanexx.user_profile_api.domain.model.*

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val failure: ProfileFailure? = null,
    val notice: SettingsNotice? = null,
)
