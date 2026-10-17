package ru.mareanex.auth_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthUserDto(
    val id: String?,
    val username: String?,
    @SerializedName("full_name") val fullName: String?,
)
