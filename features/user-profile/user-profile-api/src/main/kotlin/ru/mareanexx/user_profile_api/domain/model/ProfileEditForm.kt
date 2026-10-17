package ru.mareanexx.user_profile_api.domain.model

data class ProfileEditForm(
    val fullName: String = "",
    val username: String = "",
    val description: String = "",
)
