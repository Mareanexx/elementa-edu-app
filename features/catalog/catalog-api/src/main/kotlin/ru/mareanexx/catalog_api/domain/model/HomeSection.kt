package ru.mareanexx.catalog_api.domain.model

data class HomeSection(
    val id: String,
    val books: List<CatalogBook>,
    val sort: CatalogSort,
)
