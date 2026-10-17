package ru.mareanexx.user_profile_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class AchievementDto(
    val id: String?,
    val title: String?,
    val description: String?,
    val category: String?,
    val art: String?,
    val current: Int?,
    val target: Int?,
    val unit: String?,
    @SerializedName("unlocked_at") val unlockedAt: String?,
)
