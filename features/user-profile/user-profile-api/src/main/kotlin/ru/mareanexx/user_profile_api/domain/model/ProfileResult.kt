package ru.mareanexx.user_profile_api.domain.model

sealed interface ProfileResult<out T> {
    data class Success<T>(
        val value: T,
    ) : ProfileResult<T>

    data class Failure(
        val reason: ProfileFailure,
    ) : ProfileResult<Nothing>
}
