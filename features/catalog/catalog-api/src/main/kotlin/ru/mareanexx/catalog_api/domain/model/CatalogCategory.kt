package ru.mareanexx.catalog_api.domain.model

data class CatalogCategory(
    val id: String,
    val title: String,
    val description: String,
    val iconKey: String,
    val bookCount: Int,
)
