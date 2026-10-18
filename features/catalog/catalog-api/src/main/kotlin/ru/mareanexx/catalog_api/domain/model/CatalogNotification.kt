package ru.mareanexx.catalog_api.domain.model

import java.time.Instant

data class CatalogNotification(
    val id: String,
    val sequence: Long,
    val title: String,
    val body: String,
    val createdAt: Instant,
    val readAt: Instant?,
    val bookId: String?,
    val categoryId: String?,
)
