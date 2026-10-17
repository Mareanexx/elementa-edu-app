package ru.mareanexx.elementa

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ru.mareanexx.auth_api.domain.model.*
import ru.mareanexx.auth_api.domain.repository.AuthRepository
import ru.mareanexx.elementa.presentation.session.*
import ru.mareanexx.elementa.presentation.session.model.*

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }
    private class Repository : AuthRepository {
        override val session = MutableStateFlow<AuthSession?>(null)
        override suspend fun login(username: String, password: String) = AuthResult.Success
        override suspend fun register(fullName: String, username: String, password: String) = AuthResult.Success
        override suspend fun clearSession() { session.value = null }
    }
    @Test fun `new install opens guest flow and saved session opens main`() = runTest(dispatcher) {
        val repo = Repository()
        val model = SessionViewModel(repo)
        assertEquals(SessionState.Restoring, model.state.value)
        runCurrent()
        assertEquals(SessionState.Guest, model.state.value)
        repo.session.value = AuthSession(AuthUser("1", "chemist", "Chemist"), System.currentTimeMillis() + 3_600_000)
        runCurrent()
        assertTrue(model.state.value is SessionState.SignedIn)
        model.accept(SessionIntent.SignOut)
        runCurrent()
        assertEquals(SessionState.Guest, model.state.value)
    }
}
