package ru.mareanex.auth_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("full_name") val fullName: String,
    val username: String,
    val password: String,
)
