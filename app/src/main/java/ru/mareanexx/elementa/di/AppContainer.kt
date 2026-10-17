package ru.mareanexx.elementa.di

import android.app.Application
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.mareanex.auth_impl.data.remote.api.AuthApi
import ru.mareanex.auth_impl.data.AuthRepositoryFactory
import ru.mareanex.auth_impl.data.configureAuthTransport
import ru.mareanexx.auth_api.domain.repository.AuthRepository
import ru.mareanexx.datastore.SecureSessionStore
import ru.mareanexx.datastore.crypto.AndroidKeystoreSessionCipher
import ru.mareanexx.elementa.BuildConfig
import java.util.concurrent.TimeUnit

/** Собирает зависимости уровня приложения. Остальные слои получают доменные контракты. */
class AppContainer(
    application: Application,
) {
    val authRepository: AuthRepository by lazy {
        val gson = Gson()
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            // Регистрация не должна повторяться автоматически после сетевого сбоя.
            .retryOnConnectionFailure(false)
            .also { configureAuthTransport(application, it, BuildConfig.MOCK_AUTH) }
            .build()
        val api = Retrofit.Builder().baseUrl(BuildConfig.AUTH_BASE_URL).client(client)
            .addConverterFactory(GsonConverterFactory.create(gson)).build().create(AuthApi::class.java)
        AuthRepositoryFactory.create(api, SecureSessionStore(application, AndroidKeystoreSessionCipher()), gson)
    }
}
