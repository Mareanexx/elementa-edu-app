package ru.mareanexx.auth_api.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mareanexx.auth_api.domain.model.AuthSession
import ru.mareanexx.auth_api.domain.model.AuthResult

interface AuthRepository {
    val session: Flow<AuthSession?>
    suspend fun login(username: String, password: String): AuthResult
    suspend fun register(fullName: String, username: String, password: String): AuthResult
    /** Также вызывается после подтверждённого удаления аккаунта или отзыва сессии сервером. */
    suspend fun clearSession()
}
