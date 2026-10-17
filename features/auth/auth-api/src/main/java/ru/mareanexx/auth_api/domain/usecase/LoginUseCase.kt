package ru.mareanexx.auth_api.domain.usecase

import ru.mareanexx.auth_api.domain.model.AuthError
import ru.mareanexx.auth_api.domain.model.AuthFailure
import ru.mareanexx.auth_api.domain.model.AuthForm
import ru.mareanexx.auth_api.domain.model.AuthMode
import ru.mareanexx.auth_api.domain.model.AuthResult
import ru.mareanexx.auth_api.domain.repository.AuthRepository
import ru.mareanexx.auth_api.domain.validation.AuthValidator
import java.util.Locale

/** Проверяет обязательные данные входа и приводит логин к единому виду. Пароль не меняется. */
class LoginUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(form: AuthForm): AuthResult {
        val errors = AuthValidator.validate(form, AuthMode.Login)
        if (errors.isNotEmpty()) return AuthResult.Failure(AuthFailure(AuthError.InvalidField, errors))
        return repository.login(form.username.trim().lowercase(Locale.ROOT), form.password)
    }
}
