package ru.mareanexx.user_profile_api.domain.model

data class ProfileFailure(
    val error: ProfileError,
    val fields: Map<ProfileField, ProfileError> = emptyMap(),
)
