package ru.mareanex.auth_impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.mareanex.auth_impl.data.AuthRepositoryFactory
import ru.mareanex.auth_impl.data.remote.api.AuthApi
import ru.mareanexx.auth_api.domain.usecase.RegisterUseCase
import ru.mareanexx.auth_api.domain.model.*
import ru.mareanexx.auth_api.domain.repository.AuthRepository
import ru.mareanexx.datastore.SessionStore
import java.io.IOException

class AuthRepositoryIntegrationTest {
    private val server = MockWebServer()
    private class Store : SessionStore {
        override val data = MutableStateFlow<String?>(null)
        var saveFails = false
        override suspend fun save(value: String) { if (saveFails) throw IOException(); data.value = value }
        override suspend fun clear() { data.value = null }
    }
    private lateinit var store: Store
    private lateinit var repository: AuthRepository
    private val success = """{"access_token":"access","refresh_token":"refresh","token_type":"Bearer","expires_in":3600,"user":{"id":"1","username":"chemist","full_name":"Химик"}}"""
    @Before fun setup() {
        server.start()
        store = Store()
        val api = Retrofit.Builder().baseUrl(server.url("/")).addConverterFactory(GsonConverterFactory.create()).build().create(AuthApi::class.java)
        repository = AuthRepositoryFactory.create(api, store, now = { 1_000 })
    }
    @After fun teardown() { server.shutdown() }

    @Test fun `register sends agreed JSON and persists session before success`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(201).setBody(success))
        assertEquals(AuthResult.Success, RegisterUseCase(repository)(AuthForm(" Химик ", " CHEMIST ", "Elementa123", "Elementa123")))
        val request = server.takeRequest()
        assertEquals("/api/v1/auth/register", request.path)
        val json = request.body.readUtf8()
        assertTrue(json.contains("\"full_name\":\"Химик\""))
        assertTrue(json.contains("\"username\":\"chemist\""))
        assertFalse(json.contains("confirm"))
        assertEquals(3_601_000, repository.session.first()!!.expiresAtMillis)
    }
    @Test fun `login uses login endpoint and clears session on logout`() = runBlocking {
        server.enqueue(MockResponse().setBody(success))
        assertEquals(AuthResult.Success, repository.login("chemist", "Elementa123"))
        assertEquals("/api/v1/auth/login", server.takeRequest().path)
        repository.clearSession()
        assertNull(repository.session.first())
    }
    @Test fun `status errors are classified without exposing backend text`() = runBlocking {
        for ((status, expected) in mapOf(401 to AuthError.InvalidCredentials, 403 to AuthError.AccountDisabled,
            409 to AuthError.UsernameTaken, 422 to AuthError.InvalidField, 503 to AuthError.ServerUnavailable,
            504 to AuthError.Timeout)) {
            server.enqueue(MockResponse().setResponseCode(status).setBody("""{"error":{"code":"ANY","message":"private diagnostic"}}"""))
            val result = repository.login("chemist", "bad") as AuthResult.Failure
            assertEquals(expected, result.reason.error)
            assertNull(store.data.value)
        }
    }
    @Test fun `server validation maps only known fields`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(422).setBody("""{"error":{"code":"VALIDATION_ERROR","field_errors":{"username":"USERNAME_TAKEN","password":"WEAK_PASSWORD","internal":"secret"}}}"""))
        val reason = (repository.login("chemist", "abc") as AuthResult.Failure).reason
        assertEquals(mapOf(AuthField.Username to AuthError.UsernameTaken, AuthField.Password to AuthError.WeakPassword), reason.fields)
    }
    @Test fun `rate limit honors Retry-After`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", "12"))
        val failure = (repository.login("chemist", "abc") as AuthResult.Failure).reason
        assertEquals(AuthError.TooManyRequests, failure.error)
        assertEquals(12, failure.retryAfterSeconds)
    }
    @Test fun `incomplete success response cannot establish session`() = runBlocking {
        server.enqueue(MockResponse().setBody("{}"))
        assertEquals(AuthError.InvalidResponse, (repository.login("chemist", "abc") as AuthResult.Failure).reason.error)
        assertNull(repository.session.first())
    }
    @Test fun `failed persistence does not report login success`() = runBlocking {
        store.saveFails = true
        server.enqueue(MockResponse().setBody(success))
        assertEquals(AuthError.Storage, (repository.login("chemist", "abc") as AuthResult.Failure).reason.error)
        assertNull(repository.session.first())
    }
    @Test fun `expired stored sessions are not restored`() = runBlocking {
        store.data.value = """{"accessToken":"a","refreshToken":"r","session":{"user":{"id":"1","username":"chemist","fullName":"Химик"},"expiresAtMillis":999}}"""
        assertNull(repository.session.first())
    }
    @Test fun `corrupted stored session returns signed out`() = runBlocking {
        store.data.value = "not json"
        assertNull(repository.session.first())
    }
}
