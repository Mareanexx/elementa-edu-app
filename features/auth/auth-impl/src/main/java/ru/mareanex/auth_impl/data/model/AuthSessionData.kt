package ru.mareanex.auth_impl.data.model

import ru.mareanexx.auth_api.domain.model.AuthSession

/** Токены доступны слою данных; представление получает только доменную сессию. */
internal data class AuthSessionData(
    val accessToken: String,
    val refreshToken: String,
    val session: AuthSession,
)
