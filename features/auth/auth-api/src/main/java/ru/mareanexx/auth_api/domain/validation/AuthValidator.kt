package ru.mareanexx.auth_api.domain.validation

import ru.mareanexx.auth_api.domain.model.AuthError
import ru.mareanexx.auth_api.domain.model.AuthField
import ru.mareanexx.auth_api.domain.model.AuthForm
import ru.mareanexx.auth_api.domain.model.AuthMode

object AuthValidator {
    fun validate(form: AuthForm, mode: AuthMode): Map<AuthField, AuthError> = buildMap {
        if (form.username.isBlank()) put(AuthField.Username, AuthError.Required)
        if (form.password.isBlank()) put(AuthField.Password, AuthError.Required)
        if (mode == AuthMode.Register) {
            val name = form.fullName.trim()
            if (name.isEmpty()) put(AuthField.FullName, AuthError.Required)
            else if (name.length !in 2..80 || name.none(Char::isLetter)) put(AuthField.FullName, AuthError.InvalidName)
            if (form.username.isNotBlank() && !Regex("[a-zA-Z][a-zA-Z0-9_]{2,31}").matches(form.username.trim())) {
                put(AuthField.Username, AuthError.InvalidUsername)
            }
            if (form.password.isNotBlank() && (form.password.length !in 8..128 ||
                    form.password.none(Char::isLetter) || form.password.none(Char::isDigit))) {
                put(AuthField.Password, AuthError.WeakPassword)
            }
            if (form.confirmPassword.isEmpty()) put(AuthField.ConfirmPassword, AuthError.Required)
            else if (form.password != form.confirmPassword) put(AuthField.ConfirmPassword, AuthError.PasswordMismatch)
        }
    }
}
