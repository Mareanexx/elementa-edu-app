package ru.mareanexx.catalog_impl.presentation.model

import ru.mareanexx.catalog_api.domain.model.*

internal data class HomeUiState(
    val feed: HomeFeed? = null,
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val error: CatalogError? = null,
    val unreadCount: Int = 0,
    val actionBook: CatalogBook? = null,
    val saving: Boolean = false,
    val actionError: CatalogError? = null,
    val notice: CatalogNotice? = null,
)
