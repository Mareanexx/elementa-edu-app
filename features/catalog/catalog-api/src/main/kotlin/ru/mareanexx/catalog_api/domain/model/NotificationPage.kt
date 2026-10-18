package ru.mareanexx.catalog_api.domain.model

data class NotificationPage(
    val items: List<CatalogNotification>,
    val nextCursor: String?,
    val snapshotSequence: Long,
    val unread: UnreadState,
)
