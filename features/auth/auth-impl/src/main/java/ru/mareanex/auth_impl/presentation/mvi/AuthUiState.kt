package ru.mareanex.auth_impl.presentation.mvi

import ru.mareanexx.auth_api.domain.model.AuthForm
import ru.mareanexx.auth_api.domain.model.AuthMode
import ru.mareanexx.auth_api.domain.model.AuthError
import ru.mareanexx.auth_api.domain.model.AuthField
import ru.mareanexx.auth_api.domain.model.AuthFailure

data class AuthUiState(
    val mode: AuthMode,
    val form: AuthForm = AuthForm(),
    val errors: Map<AuthField, AuthError> = emptyMap(),
    val touched: Set<AuthField> = emptySet(),
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val failure: AuthFailure? = null,
    val retryAfterSeconds: Int = 0,
) {
    val canSubmit get() = !isLoading && retryAfterSeconds == 0
}
