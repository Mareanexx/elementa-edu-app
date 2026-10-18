package ru.mareanexx.catalog_impl.presentation.model

import ru.mareanexx.catalog_api.domain.model.*

internal data class NotificationUiState(
    val filter: NotificationFilter = NotificationFilter.All,
    val items: List<CatalogNotification> = emptyList(),
    val loaded: Boolean = false,
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val nextCursor: String? = null,
    val snapshotSequence: Long = 0,
    val error: CatalogError? = null,
    val appendFailed: Boolean = false,
    val saving: Boolean = false,
    val writeError: CatalogError? = null,
    val pendingReadId: String? = null,
    val pendingReadAll: Boolean = false,
    val pendingReadThrough: Long? = null,
    val unreadCount: Int = 0,
)
