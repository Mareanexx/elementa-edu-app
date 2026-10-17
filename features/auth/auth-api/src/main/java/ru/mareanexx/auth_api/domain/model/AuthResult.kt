package ru.mareanexx.auth_api.domain.model

sealed interface AuthResult {
    data object Success : AuthResult
    data class Failure(
        val reason: AuthFailure,
    ) : AuthResult
}
