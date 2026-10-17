package ru.mareanex.auth_impl.data.remote.dto

data class LoginRequest(
    val username: String,
    val password: String,
)
