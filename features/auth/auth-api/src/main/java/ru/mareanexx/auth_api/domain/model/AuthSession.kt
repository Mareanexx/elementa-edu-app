package ru.mareanexx.auth_api.domain.model

data class AuthSession(
    val user: AuthUser,
    val expiresAtMillis: Long,
)
