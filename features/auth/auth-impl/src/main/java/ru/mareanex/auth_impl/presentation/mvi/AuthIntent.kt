package ru.mareanex.auth_impl.presentation.mvi

import ru.mareanexx.auth_api.domain.model.AuthField

sealed interface AuthIntent {
    data class FieldChanged(
        val field: AuthField,
        val value: String,
    ) : AuthIntent
    data class FieldBlurred(
        val field: AuthField,
    ) : AuthIntent
    data class TogglePassword(
        val confirmation: Boolean = false,
    ) : AuthIntent
    data object Submit : AuthIntent
    data object DismissError : AuthIntent
}
