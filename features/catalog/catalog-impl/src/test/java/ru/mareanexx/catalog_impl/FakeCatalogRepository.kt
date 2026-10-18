package ru.mareanexx.catalog_impl

import kotlinx.coroutines.flow.MutableStateFlow
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_api.domain.repository.CatalogRepository
import ru.mareanexx.catalog_api.domain.validation.CatalogSearchRules
import java.time.Instant

internal class FakeCatalogRepository : CatalogRepository {
    override val recentQueries = MutableStateFlow(emptyList<String>())
    override val unread = MutableStateFlow<UnreadState?>(UnreadState(3, 1))
    val calls = mutableListOf<Pair<CatalogQuery, String?>>()
    val noticeCalls = mutableListOf<Pair<NotificationFilter, String?>>()
    val markAllCalls = mutableListOf<Long>()
    val readCalls = mutableListOf<String>()
    var homeCalls = 0
    var historyCalls = 0
    var getHome: suspend () -> CatalogResult<HomeFeed> = { CatalogResult.Success(CatalogFixtures.home()) }
    var getBooks: suspend (CatalogQuery, String?) -> CatalogResult<CatalogPage> = { _, _ -> CatalogResult.Success(CatalogFixtures.page()) }
    var getNotes: suspend (NotificationFilter, String?) -> CatalogResult<NotificationPage> = { _, _ -> CatalogResult.Success(NotificationPage(listOf(CatalogFixtures.note()), null, 3, UnreadState(3, 1))) }
    var receipt: suspend () -> CatalogResult<NotificationReceipt> = { CatalogResult.Success(NotificationReceipt(Instant.now(), UnreadState(0, 2))) }
    override suspend fun home(): CatalogResult<HomeFeed> { homeCalls++; return getHome() }
    override suspend fun categories() = CatalogResult.Success(listOf(CatalogFixtures.category(), CatalogFixtures.category("physical")))
    override suspend fun books(query: CatalogQuery, cursor: String?): CatalogResult<CatalogPage> { calls += query to cursor; return getBooks(query, cursor) }
    override suspend fun suggestions() = CatalogResult.Success(listOf("Органическая химия"))
    override suspend fun notifications(filter: NotificationFilter, cursor: String?): CatalogResult<NotificationPage> { noticeCalls += filter to cursor; return getNotes(filter, cursor) }
    override suspend fun markRead(id: String): CatalogResult<NotificationReceipt> { readCalls += id; return receipt() }
    override suspend fun markAllRead(throughSequence: Long): CatalogResult<NotificationReceipt> { markAllCalls += throughSequence; return receipt() }
    override suspend fun rememberQuery(value: String): CatalogResult<Unit> {
        historyCalls++
        recentQueries.value = (listOf(value) + recentQueries.value).distinctBy(CatalogSearchRules::key)
        return CatalogResult.Success(Unit)
    }
    override suspend fun removeQuery(value: String): CatalogResult<Unit> { recentQueries.value = recentQueries.value - value; return CatalogResult.Success(Unit) }
    override suspend fun clearHistory(): CatalogResult<Unit> { recentQueries.value = emptyList(); return CatalogResult.Success(Unit) }
}
