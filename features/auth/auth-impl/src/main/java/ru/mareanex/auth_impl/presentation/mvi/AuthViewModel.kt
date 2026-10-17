package ru.mareanex.auth_impl.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.mareanexx.auth_api.domain.model.AuthMode
import ru.mareanexx.auth_api.domain.validation.AuthValidator
import ru.mareanexx.auth_api.domain.model.*
import ru.mareanexx.auth_api.domain.usecase.LoginUseCase
import ru.mareanexx.auth_api.domain.usecase.RegisterUseCase

class AuthViewModel(
    private val login: LoginUseCase,
    private val register: RegisterUseCase,
    mode: AuthMode,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthUiState(mode))
    val state = mutableState.asStateFlow()

    fun accept(intent: AuthIntent) {
        if (mutableState.value.isLoading) return
        when (intent) {
            is AuthIntent.FieldChanged -> mutableState.update { AuthReducer.changeField(it, intent.field, intent.value) }
            is AuthIntent.FieldBlurred -> mutableState.update {
                val errors = AuthValidator.validate(it.form, it.mode)
                val fieldError = errors[intent.field] ?: it.errors[intent.field]
                it.copy(touched = it.touched + intent.field,
                    errors = it.errors - intent.field +
                        if (fieldError == null) emptyMap() else mapOf(intent.field to fieldError))
            }
            is AuthIntent.TogglePassword -> mutableState.update {
                if (intent.confirmation) it.copy(confirmPasswordVisible = !it.confirmPasswordVisible)
                else it.copy(passwordVisible = !it.passwordVisible)
            }
            AuthIntent.Submit -> submit()
            AuthIntent.DismissError -> mutableState.update { it.copy(failure = null) }
        }
    }

    private fun submit() {
        val current = mutableState.value
        if (!current.canSubmit) return
        val errors = AuthValidator.validate(current.form, current.mode)
        mutableState.update { it.copy(errors = errors, touched = AuthField.entries.toSet(), failure = null) }
        if (errors.isNotEmpty()) return
        // Блокируем повторную отправку до запуска корутины, включая быстрые нажатия.
        mutableState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val form = current.form
            val result = try {
                if (current.mode == AuthMode.Login) login(form) else register(form)
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (_: Exception) { AuthResult.Failure(AuthFailure(AuthError.ServerUnavailable)) }
            when (result) {
                AuthResult.Success -> mutableState.update {
                    it.copy(form = it.form.copy(password = "", confirmPassword = ""), isLoading = false,
                        passwordVisible = false, confirmPasswordVisible = false)
                }
                is AuthResult.Failure -> {
                    val reason = result.reason
                    mutableState.update { it.copy(isLoading = false, failure = reason,
                        errors = reason.fields, retryAfterSeconds = reason.retryAfterSeconds) }
                    repeat(reason.retryAfterSeconds) {
                        delay(1_000)
                        mutableState.update { it.copy(retryAfterSeconds = (it.retryAfterSeconds - 1).coerceAtLeast(0)) }
                    }
                }
            }
        }
    }
}
