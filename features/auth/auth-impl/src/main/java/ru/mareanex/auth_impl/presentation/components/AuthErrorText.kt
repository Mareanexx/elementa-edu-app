package ru.mareanex.auth_impl.presentation.components

import androidx.annotation.StringRes
import ru.mareanexx.auth_api.domain.model.AuthError
import ru.mareanexx.auth_impl.R

@StringRes
internal fun authErrorText(error: AuthError): Int = when (error) {
    AuthError.Required -> R.string.auth_required
    AuthError.InvalidName -> R.string.auth_invalid_name
    AuthError.InvalidUsername -> R.string.auth_invalid_username
    AuthError.WeakPassword -> R.string.auth_weak_password
    AuthError.PasswordMismatch -> R.string.auth_password_mismatch
    AuthError.InvalidCredentials -> R.string.auth_invalid_credentials
    AuthError.UsernameTaken -> R.string.auth_username_taken
    AuthError.InvalidField -> R.string.auth_invalid_field
    AuthError.AccountDisabled -> R.string.auth_account_disabled
    AuthError.Offline -> R.string.auth_offline
    AuthError.Timeout -> R.string.auth_timeout
    AuthError.ServerUnavailable -> R.string.auth_server_error
    AuthError.TooManyRequests -> R.string.auth_too_many_requests
    AuthError.InvalidResponse -> R.string.auth_invalid_response
    AuthError.Storage -> R.string.auth_storage_error
}
