package ru.mareanex.auth_impl.data.remote.mapper

import ru.mareanex.auth_impl.data.model.AuthSessionData
import ru.mareanex.auth_impl.data.remote.dto.AuthResponse
import ru.mareanexx.auth_api.domain.model.AuthSession
import ru.mareanexx.auth_api.domain.model.AuthUser

internal class AuthResponseMapper(
    private val now: () -> Long = System::currentTimeMillis,
) {
    fun map(body: AuthResponse): AuthSessionData? {
        val user = body.user ?: return null
        val expiresIn = body.expiresIn
        if (body.accessToken.isNullOrBlank() || body.refreshToken.isNullOrBlank() ||
            !body.tokenType.equals("Bearer", ignoreCase = true) || expiresIn == null || expiresIn !in 1..31_536_000 ||
            user.id.isNullOrBlank() || user.username.isNullOrBlank() || user.fullName.isNullOrBlank()) return null
        return AuthSessionData(body.accessToken, body.refreshToken,
            AuthSession(AuthUser(user.id, user.username, user.fullName), now() + expiresIn * 1000))
    }
}
