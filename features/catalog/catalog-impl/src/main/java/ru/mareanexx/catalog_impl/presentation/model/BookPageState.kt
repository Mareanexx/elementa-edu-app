package ru.mareanexx.catalog_impl.presentation.model

import ru.mareanexx.catalog_api.domain.model.*

internal data class BookPageState(
    val items: List<CatalogBook> = emptyList(),
    val loaded: Boolean = false,
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val error: CatalogError? = null,
    val nextCursor: String? = null,
    val total: Int = 0,
    val appendFailed: Boolean = false,
)
