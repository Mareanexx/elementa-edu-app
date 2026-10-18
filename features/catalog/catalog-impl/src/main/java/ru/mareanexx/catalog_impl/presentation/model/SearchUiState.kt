package ru.mareanexx.catalog_impl.presentation.model

import ru.mareanexx.catalog_api.domain.model.*

internal data class SearchUiState(
    val query: String = "",
    val page: BookPageState = BookPageState(),
    val history: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val suggestionsLoaded: Boolean = false,
    val suggestionError: CatalogError? = null,
    val historyError: CatalogError? = null,
)
