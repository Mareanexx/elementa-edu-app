package ru.mareanexx.catalog_impl.data.mock

internal data class DemoNotificationCursor(
    val owner: String,
    val filter: String,
    val snapshot: Long,
    val before: Long,
)
