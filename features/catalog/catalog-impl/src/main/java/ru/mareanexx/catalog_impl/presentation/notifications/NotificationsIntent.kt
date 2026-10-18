package ru.mareanexx.catalog_impl.presentation.notifications

import ru.mareanexx.catalog_api.domain.model.*

internal sealed interface NotificationsIntent {
    data object Enter : NotificationsIntent
    data class Filter(
        val value: NotificationFilter,
    ) : NotificationsIntent
    data class Open(
        val value: CatalogNotification,
    ) : NotificationsIntent
    data object MarkAll : NotificationsIntent
    data object RetryWrite : NotificationsIntent
    data object Refresh : NotificationsIntent
    data object More : NotificationsIntent
    data object Retry : NotificationsIntent
}
