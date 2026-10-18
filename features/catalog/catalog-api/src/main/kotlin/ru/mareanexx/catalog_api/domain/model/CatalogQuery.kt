package ru.mareanexx.catalog_api.domain.model

data class CatalogQuery(
    val categoryId: String? = null,
    val search: String = "",
    val sort: CatalogSort = CatalogSort.Recommended,
)
