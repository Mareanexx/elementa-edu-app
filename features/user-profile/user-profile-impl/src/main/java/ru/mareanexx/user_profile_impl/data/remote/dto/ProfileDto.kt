package ru.mareanexx.user_profile_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class ProfileDto(
    val id: String?,
    @SerializedName("full_name") val fullName: String?,
    val username: String?,
    val description: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    val statistics: StatisticsDto?,
)
