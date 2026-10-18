package ru.mareanexx.book_api.domain.model

sealed interface BookResult<out T> {
    data class Success<T>(
        val value: T,
    ) : BookResult<T>
    data class Failure(
        val error: BookError,
    ) : BookResult<Nothing>
}
