package ru.mareanexx.user_profile_impl.presentation.profile

import ru.mareanexx.user_profile_api.domain.model.*

sealed interface ProfileIntent {
    data object Reload : ProfileIntent
    data object BeginEdit : ProfileIntent
    data class ChangeField(
        val field: ProfileField,
        val value: String,
    ) : ProfileIntent
    data class BlurField(
        val field: ProfileField,
    ) : ProfileIntent
    data object Save : ProfileIntent
    data class UploadAvatar(
        val image: AvatarUpload,
    ) : ProfileIntent
    data object RemoveAvatar : ProfileIntent
    data object DismissError : ProfileIntent
}
