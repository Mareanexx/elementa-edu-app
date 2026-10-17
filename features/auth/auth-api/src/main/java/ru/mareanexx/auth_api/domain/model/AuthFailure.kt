package ru.mareanexx.auth_api.domain.model

data class AuthFailure(
    val error: AuthError,
    val fields: Map<AuthField, AuthError> = emptyMap(),
    val retryAfterSeconds: Int = 0,
)
