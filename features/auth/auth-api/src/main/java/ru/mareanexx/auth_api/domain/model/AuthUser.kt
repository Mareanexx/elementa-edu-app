package ru.mareanexx.auth_api.domain.model

data class AuthUser(
    val id: String,
    val username: String,
    val fullName: String,
)
