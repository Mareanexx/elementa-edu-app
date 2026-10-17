package ru.mareanex.auth_impl.data.model

import ru.mareanexx.auth_api.domain.model.AuthFailure

internal sealed interface RemoteAuthResult {
    data class Success(
        val session: AuthSessionData,
    ) : RemoteAuthResult
    data class Failure(
        val reason: AuthFailure,
    ) : RemoteAuthResult
}
