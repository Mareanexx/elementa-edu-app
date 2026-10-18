package ru.mareanexx.catalog_impl.presentation.model

import ru.mareanexx.catalog_api.domain.model.*

internal data class CatalogUiState(
    val categories: List<CatalogCategory> = emptyList(),
    val categoriesLoaded: Boolean = false,
    val categoryError: CatalogError? = null,
    val selectedId: String? = null,
    val sort: CatalogSort = CatalogSort.Recommended,
    val pages: Map<String, BookPageState> = emptyMap(),
) {
    val page: BookPageState get() = pages[selectedId.orEmpty()] ?: BookPageState()
}
