package ru.mareanexx.auth_api.domain.model

enum class AuthError {
    Required, InvalidName, InvalidUsername, WeakPassword, PasswordMismatch,
    InvalidCredentials, UsernameTaken, InvalidField, AccountDisabled,
    Offline, Timeout, ServerUnavailable, TooManyRequests, InvalidResponse, Storage,
}
