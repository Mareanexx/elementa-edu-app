package ru.mareanexx.catalog_api.domain.model

import java.time.Instant

data class NotificationReceipt(
    val readAt: Instant,
    val unread: UnreadState,
)
