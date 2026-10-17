package ru.mareanexx.user_profile_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class ProfileErrorDto(
    val code: String?,
    @SerializedName("field_errors") val fields: Map<String, String>?,
)
