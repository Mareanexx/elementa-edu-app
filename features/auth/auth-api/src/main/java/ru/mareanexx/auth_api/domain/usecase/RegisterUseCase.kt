package ru.mareanexx.auth_api.domain.usecase

import ru.mareanexx.auth_api.domain.model.AuthError
import ru.mareanexx.auth_api.domain.model.AuthFailure
import ru.mareanexx.auth_api.domain.model.AuthForm
import ru.mareanexx.auth_api.domain.model.AuthMode
import ru.mareanexx.auth_api.domain.model.AuthResult
import ru.mareanexx.auth_api.domain.repository.AuthRepository
import ru.mareanexx.auth_api.domain.validation.AuthValidator
import java.util.Locale

/** Проверяет правила регистрации до обращения к репозиторию. Подтверждение пароля остаётся на клиенте. */
class RegisterUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(form: AuthForm): AuthResult {
        val errors = AuthValidator.validate(form, AuthMode.Register)
        if (errors.isNotEmpty()) return AuthResult.Failure(AuthFailure(AuthError.InvalidField, errors))
        return repository.register(form.fullName.trim(), form.username.trim().lowercase(Locale.ROOT), form.password)
    }
}
