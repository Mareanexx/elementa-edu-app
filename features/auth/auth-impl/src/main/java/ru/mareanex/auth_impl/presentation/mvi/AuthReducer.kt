package ru.mareanex.auth_impl.presentation.mvi

import ru.mareanexx.auth_api.domain.model.AuthField

object AuthReducer {
    fun changeField(state: AuthUiState, field: AuthField, value: String): AuthUiState {
        val form = when (field) {
            AuthField.FullName -> state.form.copy(fullName = value)
            AuthField.Username -> state.form.copy(username = value)
            AuthField.Password -> state.form.copy(password = value)
            AuthField.ConfirmPassword -> state.form.copy(confirmPassword = value)
        }
        val validation = ru.mareanexx.auth_api.domain.validation.AuthValidator.validate(form, state.mode)
        val affected = if (field == AuthField.Password) setOf(field, AuthField.ConfirmPassword) else setOf(field)
        val errors = state.errors.toMutableMap()
        affected.forEach {
            errors.remove(it)
            if (it in state.touched) validation[it]?.let { error -> errors[it] = error }
        }
        return state.copy(form = form, errors = errors, failure = null)
    }
}
