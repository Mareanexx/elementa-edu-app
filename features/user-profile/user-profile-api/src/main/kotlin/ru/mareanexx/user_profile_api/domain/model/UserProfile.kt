package ru.mareanexx.user_profile_api.domain.model

data class UserProfile(
    val id: String,
    val fullName: String,
    val username: String,
    val description: String,
    val avatarUrl: String?,
    val statistics: ProfileStatistics,
)
