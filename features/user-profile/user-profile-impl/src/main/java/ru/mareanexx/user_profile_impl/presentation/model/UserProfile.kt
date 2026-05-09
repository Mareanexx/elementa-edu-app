package ru.mareanexx.user_profile_impl.presentation.model

import androidx.annotation.DrawableRes

data class UserProfile(
    val mainData: ProfileMainData,
    val statistics: ProfileStatistics,
    val achievements: ProfileAchievements,
)

/**
 * Презентационная модель основной информации в профиле пользователя
 */
data class ProfileMainData(
    val fullName: String,
    val username: String,
    val avatarUrl: String,
    val description: String,
)

data class ProfileAchievements(
    val list: List<Achievement>,
)

data class Achievement(
    @param:DrawableRes val image: Int,
    val title: String,
    val description: String? = null,
    val receiptDate: String,
)

data class ProfileStatistics(
    val readBooksNumber: Int,
    val publishedNotesNumber: Int,
    val testsPassedNumber: Int,
)
