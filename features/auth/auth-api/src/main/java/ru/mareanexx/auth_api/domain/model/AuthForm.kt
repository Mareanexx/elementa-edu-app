package ru.mareanexx.auth_api.domain.model

data class AuthForm(
    val fullName: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
)
