package ru.mareanex.auth_impl.data.remote.source

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import ru.mareanex.auth_impl.data.model.RemoteAuthResult
import ru.mareanex.auth_impl.data.remote.api.AuthApi
import ru.mareanex.auth_impl.data.remote.dto.AuthResponse
import ru.mareanex.auth_impl.data.remote.dto.LoginRequest
import ru.mareanex.auth_impl.data.remote.dto.RegisterRequest
import ru.mareanex.auth_impl.data.remote.mapper.AuthErrorMapper
import ru.mareanex.auth_impl.data.remote.mapper.AuthResponseMapper
import ru.mareanexx.auth_api.domain.model.AuthError
import ru.mareanexx.auth_api.domain.model.AuthFailure
import java.io.IOException
import java.net.SocketTimeoutException

/** Скрывает Retrofit, HTTP-статусы и транспортные ошибки от репозитория. */
internal class RetrofitAuthRemoteDataSource(
    private val api: AuthApi,
    private val responseMapper: AuthResponseMapper,
    private val errorMapper: AuthErrorMapper,
) {
    suspend fun login(username: String, password: String) = request { api.login(LoginRequest(username, password)) }

    suspend fun register(fullName: String, username: String, password: String) = request {
        api.register(RegisterRequest(fullName, username, password))
    }

    private suspend fun request(call: suspend () -> Response<AuthResponse>): RemoteAuthResult = withContext(Dispatchers.IO) {
        val response = try { call()
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: SocketTimeoutException) { return@withContext failure(AuthError.Timeout)
        } catch (_: IOException) { return@withContext failure(AuthError.Offline)
        } catch (_: RuntimeException) { return@withContext failure(AuthError.InvalidResponse) }
        if (!response.isSuccessful) return@withContext RemoteAuthResult.Failure(errorMapper.map(response))
        val session = response.body()?.let(responseMapper::map) ?: return@withContext failure(AuthError.InvalidResponse)
        RemoteAuthResult.Success(session)
    }

    private fun failure(error: AuthError) = RemoteAuthResult.Failure(AuthFailure(error))
}
