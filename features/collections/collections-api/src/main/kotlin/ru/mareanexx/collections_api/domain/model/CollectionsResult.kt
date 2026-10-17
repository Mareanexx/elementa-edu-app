package ru.mareanexx.collections_api.domain.model

sealed interface CollectionsResult<out T> {
    data class Success<T>(
        val value: T,
    ) : CollectionsResult<T>
    data class Failure(
        val error: CollectionsError,
    ) : CollectionsResult<Nothing>
}
