package ru.mareanexx.auth_api.domain.repository

/** Даёт сетевому слою действующий токен, не раскрывая формат хранилища сессии. */
fun interface AccessTokenProvider {
    suspend fun getAccessToken(): String?
}
