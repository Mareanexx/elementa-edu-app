package ru.mareanexx.user_library_api.domain.model

sealed interface LibraryResult<out T> {
    data class Success<T>(
        val value: T,
    ) : LibraryResult<T>
    data class Failure(
        val error: LibraryError,
    ) : LibraryResult<Nothing>
}
