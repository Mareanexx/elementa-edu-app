package ru.mareanex.auth_impl.data.repository

import kotlinx.coroutines.CancellationException
import ru.mareanex.auth_impl.data.local.AuthSessionLocalDataSource
import ru.mareanex.auth_impl.data.model.RemoteAuthResult
import ru.mareanex.auth_impl.data.remote.source.RetrofitAuthRemoteDataSource
import ru.mareanexx.auth_api.domain.model.AuthError
import ru.mareanexx.auth_api.domain.model.AuthFailure
import ru.mareanexx.auth_api.domain.model.AuthResult
import ru.mareanexx.auth_api.domain.repository.AuthRepository

/** Согласует получение и сохранение сессии. Формат HTTP и способ шифрования ему неизвестны. */
internal class DefaultAuthRepository(
    private val remote: RetrofitAuthRemoteDataSource,
    private val local: AuthSessionLocalDataSource,
) : AuthRepository {
    override val session = local.session
    override suspend fun clearSession() = local.clear()
    override suspend fun login(username: String, password: String) = complete(remote.login(username, password))
    override suspend fun register(fullName: String, username: String, password: String) =
        complete(remote.register(fullName, username, password))

    private suspend fun complete(result: RemoteAuthResult): AuthResult = when (result) {
        is RemoteAuthResult.Failure -> AuthResult.Failure(result.reason)
        is RemoteAuthResult.Success -> {
            try {
                local.save(result.session)
                AuthResult.Success
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (_: Exception) { AuthResult.Failure(AuthFailure(AuthError.Storage)) }
        }
    }
}
