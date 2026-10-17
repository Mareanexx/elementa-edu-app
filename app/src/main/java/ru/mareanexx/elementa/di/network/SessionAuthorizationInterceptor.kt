package ru.mareanexx.elementa.di.network

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import ru.mareanexx.auth_api.domain.repository.AccessTokenProvider

internal class SessionAuthorizationInterceptor(
    private val tokens: AccessTokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokens.getAccessToken() }
        val request = chain.request().newBuilder().apply {
            token?.let { header("Authorization", "Bearer $it") }
        }.build()
        return chain.proceed(request)
    }
}
