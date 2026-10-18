package ru.mareanexx.catalog_api.domain.model

sealed interface CatalogResult<out T> {
    data class Success<T>(
        val value: T,
    ) : CatalogResult<T>
    data class Failure(
        val error: CatalogError,
    ) : CatalogResult<Nothing>
}
