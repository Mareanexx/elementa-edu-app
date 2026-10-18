package ru.mareanexx.reader_api.domain.model

sealed interface ReaderResult<out T> {
    data class Success<T>(
        val value: T,
    ) : ReaderResult<T>
    data class Failure(
        val error: ReaderError,
    ) : ReaderResult<Nothing>
}
