package ru.mareanexx.catalog_impl.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_api.domain.repository.CatalogRepository
import ru.mareanexx.catalog_impl.data.local.CatalogSearchHistory
import ru.mareanexx.catalog_impl.data.remote.source.CatalogRemoteDataSource

internal class DefaultCatalogRepository(
    private val remote: CatalogRemoteDataSource,
    private val history: CatalogSearchHistory,
) : CatalogRepository {
    override val recentQueries = history.queries
    private val mutableUnread = MutableStateFlow<UnreadState?>(null)
    override val unread = mutableUnread.asStateFlow()
    private fun accept(value: UnreadState) = mutableUnread.update { previous -> if (previous == null || value.revision >= previous.revision) value else previous }
    override suspend fun home() = remote.home().also { if (it is CatalogResult.Success) accept(it.value.unread) }
    override suspend fun categories() = remote.categories()
    override suspend fun books(query: CatalogQuery, cursor: String?) = remote.books(query, cursor)
    override suspend fun suggestions() = remote.suggestions()
    override suspend fun notifications(filter: NotificationFilter, cursor: String?) = remote.notifications(filter, cursor).also { if (it is CatalogResult.Success) accept(it.value.unread) }
    override suspend fun markRead(id: String) = remote.markRead(id).also { if (it is CatalogResult.Success) accept(it.value.unread) }
    override suspend fun markAllRead(throughSequence: Long) = remote.markAllRead(throughSequence).also { if (it is CatalogResult.Success) accept(it.value.unread) }
    override suspend fun rememberQuery(value: String) = history.remember(value)
    override suspend fun removeQuery(value: String) = history.remove(value)
    override suspend fun clearHistory() = history.clear()
}
