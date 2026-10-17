package ru.mareanex.auth_impl

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ru.mareanexx.auth_api.domain.model.AuthMode
import ru.mareanex.auth_impl.presentation.mvi.*
import ru.mareanexx.auth_api.domain.usecase.LoginUseCase
import ru.mareanexx.auth_api.domain.usecase.RegisterUseCase
import ru.mareanexx.auth_api.domain.model.*
import ru.mareanexx.auth_api.domain.repository.AuthRepository

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private class Repository : AuthRepository {
        override val session = MutableStateFlow<AuthSession?>(null)
        var calls = 0
        var response: AuthResult = AuthResult.Success
        var gate: CompletableDeferred<Unit>? = null
        var lastPassword = ""
        override suspend fun login(username: String, password: String): AuthResult {
            calls++; lastPassword = password; gate?.await(); return response
        }
        override suspend fun register(fullName: String, username: String, password: String) = login(username, password)
        override suspend fun clearSession() { session.value = null }
    }
    private fun createModel(repo: AuthRepository, mode: AuthMode) = AuthViewModel(LoginUseCase(repo), RegisterUseCase(repo), mode)

    private fun fill(model: AuthViewModel) {
        model.accept(AuthIntent.FieldChanged(AuthField.Username, "chemist"))
        model.accept(AuthIntent.FieldChanged(AuthField.Password, "Elementa123"))
    }

    @Test fun `invalid form never sends a request`() = runTest(dispatcher) {
        val repo = Repository()
        val model = createModel(repo, AuthMode.Register)
        model.accept(AuthIntent.Submit)
        runCurrent()
        assertEquals(0, repo.calls)
        assertEquals(4, model.state.value.errors.size)
    }
    @Test fun `duplicate submit and editing are blocked while request is running`() = runTest(dispatcher) {
        val repo = Repository().apply { gate = CompletableDeferred() }
        val model = createModel(repo, AuthMode.Login)
        fill(model)
        model.accept(AuthIntent.Submit)
        model.accept(AuthIntent.Submit)
        model.accept(AuthIntent.FieldChanged(AuthField.Username, "another"))
        runCurrent()
        assertTrue(model.state.value.isLoading)
        assertEquals("chemist", model.state.value.form.username)
        assertEquals(1, repo.calls)
        repo.gate!!.complete(Unit)
        runCurrent()
        assertFalse(model.state.value.isLoading)
        assertEquals("", model.state.value.form.password)
    }
    @Test fun `server field conflict is shown and can be corrected`() = runTest(dispatcher) {
        val repo = Repository().apply {
            response = AuthResult.Failure(AuthFailure(AuthError.UsernameTaken, mapOf(AuthField.Username to AuthError.UsernameTaken)))
        }
        val model = createModel(repo, AuthMode.Login)
        fill(model); model.accept(AuthIntent.Submit); runCurrent()
        assertEquals(AuthError.UsernameTaken, model.state.value.errors[AuthField.Username])
        model.accept(AuthIntent.FieldBlurred(AuthField.Username))
        assertEquals(AuthError.UsernameTaken, model.state.value.errors[AuthField.Username])
        model.accept(AuthIntent.FieldChanged(AuthField.Username, "chemist2"))
        assertNull(model.state.value.errors[AuthField.Username])
        assertNull(model.state.value.failure)
    }
    @Test fun `rate limit blocks retries until countdown completes`() = runTest(dispatcher) {
        val repo = Repository().apply { response = AuthResult.Failure(AuthFailure(AuthError.TooManyRequests, retryAfterSeconds = 3)) }
        val model = createModel(repo, AuthMode.Login)
        fill(model); model.accept(AuthIntent.Submit); runCurrent()
        assertEquals(3, model.state.value.retryAfterSeconds)
        model.accept(AuthIntent.Submit); runCurrent()
        assertEquals(1, repo.calls)
        advanceTimeBy(3_000); runCurrent()
        assertTrue(model.state.value.canSubmit)
    }
    @Test fun `password including surrounding spaces is passed unchanged`() = runTest(dispatcher) {
        val repo = Repository()
        val model = createModel(repo, AuthMode.Login)
        fill(model)
        model.accept(AuthIntent.FieldChanged(AuthField.Password, " Elementa123 "))
        model.accept(AuthIntent.Submit); runCurrent()
        assertEquals(" Elementa123 ", repo.lastPassword)
    }
}
