package ru.mareanexx.catalog_api.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.mareanexx.catalog_api.domain.model.*

interface CatalogRepository {
    val recentQueries: StateFlow<List<String>>
    val unread: StateFlow<UnreadState?>
    suspend fun home(): CatalogResult<HomeFeed>
    suspend fun categories(): CatalogResult<List<CatalogCategory>>
    suspend fun books(query: CatalogQuery, cursor: String? = null): CatalogResult<CatalogPage>
    suspend fun suggestions(): CatalogResult<List<String>>
    suspend fun notifications(filter: NotificationFilter, cursor: String? = null): CatalogResult<NotificationPage>
    suspend fun markRead(id: String): CatalogResult<NotificationReceipt>
    suspend fun markAllRead(throughSequence: Long): CatalogResult<NotificationReceipt>
    suspend fun rememberQuery(value: String): CatalogResult<Unit>
    suspend fun removeQuery(value: String): CatalogResult<Unit>
    suspend fun clearHistory(): CatalogResult<Unit>
}
