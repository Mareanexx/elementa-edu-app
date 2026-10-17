package ru.mareanexx.auth_api.domain.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.mareanexx.auth_api.domain.model.AuthField
import ru.mareanexx.auth_api.domain.model.AuthForm
import ru.mareanexx.auth_api.domain.model.AuthResult
import ru.mareanexx.auth_api.domain.model.AuthSession
import ru.mareanexx.auth_api.domain.repository.AuthRepository

class AuthUseCasesTest {
    private val repository = RecordingRepository()

    @Test
    fun `пустой вход не обращается к репозиторию`() = runTest {
        val result = LoginUseCase(repository)(AuthForm()) as AuthResult.Failure

        assertEquals(setOf(AuthField.Username, AuthField.Password), result.reason.fields.keys)
        assertTrue(repository.calls.isEmpty())
    }

    @Test
    fun `вход нормализует логин и сохраняет старый пароль без изменений`() = runTest {
        val result = LoginUseCase(repository)(AuthForm(username = " CHEMIST ", password = " x "))

        assertEquals(AuthResult.Success, result)
        assertEquals(listOf(listOf("chemist", " x ")), repository.calls)
    }

    @Test
    fun `регистрация нормализует имя и логин сохраняя пробелы в пароле`() = runTest {
        val form = AuthForm(" Мария Химик ", " NEW_CHEMIST ", " Elementa123 ", " Elementa123 ")

        assertEquals(AuthResult.Success, RegisterUseCase(repository)(form))
        assertEquals(listOf(listOf("Мария Химик", "new_chemist", " Elementa123 ")), repository.calls)
    }

    @Test
    fun `несовпадение паролей останавливает регистрацию до репозитория`() = runTest {
        val form = AuthForm("Мария Химик", "new_chemist", "Elementa123", "OtherPass123")
        val result = RegisterUseCase(repository)(form) as AuthResult.Failure

        assertEquals(setOf(AuthField.ConfirmPassword), result.reason.fields.keys)
        assertTrue(repository.calls.isEmpty())
    }

    private class RecordingRepository : AuthRepository {
        override val session = MutableStateFlow<AuthSession?>(null)
        val calls = mutableListOf<List<String>>()

        override suspend fun login(username: String, password: String): AuthResult {
            calls += listOf(username, password)
            return AuthResult.Success
        }

        override suspend fun register(fullName: String, username: String, password: String): AuthResult {
            calls += listOf(fullName, username, password)
            return AuthResult.Success
        }

        override suspend fun clearSession() = Unit
    }
}
