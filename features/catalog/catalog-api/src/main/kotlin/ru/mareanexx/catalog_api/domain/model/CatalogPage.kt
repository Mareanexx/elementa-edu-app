package ru.mareanexx.catalog_api.domain.model

data class CatalogPage(
    val items: List<CatalogBook>,
    val nextCursor: String?,
    val total: Int,
)
