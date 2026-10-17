package ru.mareanex.auth_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiError(
    val code: String?,
    @SerializedName("field_errors") val fieldErrors: Map<String, String>?,
    @SerializedName("retry_after_seconds") val retryAfterSeconds: Int?,
)
