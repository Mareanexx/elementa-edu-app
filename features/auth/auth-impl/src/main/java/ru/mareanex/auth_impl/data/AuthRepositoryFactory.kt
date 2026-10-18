package ru.mareanex.auth_impl.data

import com.google.gson.Gson
import ru.mareanex.auth_impl.data.local.AuthSessionLocalDataSource
import ru.mareanex.auth_impl.data.remote.api.AuthApi
import ru.mareanex.auth_impl.data.remote.mapper.AuthErrorMapper
import ru.mareanex.auth_impl.data.remote.mapper.AuthResponseMapper
import ru.mareanex.auth_impl.data.remote.source.RetrofitAuthRemoteDataSource
import ru.mareanex.auth_impl.data.repository.DefaultAuthRepository
import ru.mareanexx.auth_api.domain.repository.AccessTokenProvider
import ru.mareanexx.auth_api.domain.repository.AuthRepository
import ru.mareanexx.datastore.SessionStore

/** Собирает внутренние реализации слоя данных и возвращает только доменный контракт. */
object AuthRepositoryFactory {
    fun tokenProvider(store: SessionStore, userId: String? = null): AccessTokenProvider {
        val local = AuthSessionLocalDataSource(store, Gson())
        return AccessTokenProvider { local.accessToken(userId) }
    }

    fun create(api: AuthApi, store: SessionStore, gson: Gson = Gson(), now: () -> Long = System::currentTimeMillis): AuthRepository {
        val remote = RetrofitAuthRemoteDataSource(api, AuthResponseMapper(now), AuthErrorMapper(gson))
        val local = AuthSessionLocalDataSource(store, gson, now)
        return DefaultAuthRepository(remote, local)
    }
}
