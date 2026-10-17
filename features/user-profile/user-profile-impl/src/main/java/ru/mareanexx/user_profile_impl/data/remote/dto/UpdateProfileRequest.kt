package ru.mareanexx.user_profile_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class UpdateProfileRequest(
    @SerializedName("full_name") val fullName: String,
    val username: String,
    val description: String,
)
