package ru.mareanex.auth_impl.data.remote.mapper

import com.google.gson.Gson
import com.google.gson.JsonParseException
import retrofit2.Response
import ru.mareanex.auth_impl.data.remote.dto.ApiErrorEnvelope
import ru.mareanexx.auth_api.domain.model.AuthError
import ru.mareanexx.auth_api.domain.model.AuthFailure
import ru.mareanexx.auth_api.domain.model.AuthField
import java.io.IOException

internal class AuthErrorMapper(
    private val gson: Gson,
) {
    fun map(response: Response<*>): AuthFailure {
        val error = try {
            response.errorBody()?.use { gson.fromJson(it.string(), ApiErrorEnvelope::class.java)?.error }
        } catch (_: JsonParseException) { null } catch (_: IOException) { null }
        val fields = error?.fieldErrors.orEmpty().mapNotNull { (key, code) ->
            val field = when (key) {
                "full_name" -> AuthField.FullName
                "username" -> AuthField.Username
                "password" -> AuthField.Password
                else -> null
            }
            field?.let { it to when (code) {
                "USERNAME_TAKEN" -> AuthError.UsernameTaken
                "WEAK_PASSWORD" -> AuthError.WeakPassword
                else -> AuthError.InvalidField
            } }
        }.toMap()
        val kind = when (response.code()) {
            401 -> AuthError.InvalidCredentials
            403 -> AuthError.AccountDisabled
            409 -> AuthError.UsernameTaken
            400, 422 -> AuthError.InvalidField
            408, 504 -> AuthError.Timeout
            429 -> AuthError.TooManyRequests
            in 500..599 -> AuthError.ServerUnavailable
            else -> AuthError.InvalidResponse
        }
        val retry = (response.headers()["Retry-After"]?.toIntOrNull()
            ?: error?.retryAfterSeconds ?: 30).coerceIn(1, 300)
        return AuthFailure(kind,
            if (kind == AuthError.UsernameTaken) fields + (AuthField.Username to kind) else fields,
            if (kind == AuthError.TooManyRequests) retry else 0)
    }
}
