package ru.mareanexx.catalog_impl.presentation.catalog

import ru.mareanexx.catalog_api.domain.model.CatalogSort

internal sealed interface CatalogIntent {
    data class Open(
        val categoryId: String? = null,
        val sort: CatalogSort = CatalogSort.Recommended,
    ) : CatalogIntent
    data class Select(
        val id: String?,
    ) : CatalogIntent
    data class Sort(
        val value: CatalogSort,
    ) : CatalogIntent
    data object Refresh : CatalogIntent
    data object More : CatalogIntent
    data object Retry : CatalogIntent
}
