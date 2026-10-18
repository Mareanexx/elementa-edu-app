package ru.mareanexx.catalog_impl.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_api.domain.validation.CatalogSearchRules

internal class CatalogSearchHistory(
    context: Context,
    userId: String,
) {
    private val preferences = context.getSharedPreferences("catalog_search_history", Context.MODE_PRIVATE)
    private val key = "queries_$userId"
    private val mutex = Mutex()
    private val mutable = MutableStateFlow(runCatching {
        val array = JSONArray(preferences.getString(key, "[]"))
        (0 until array.length()).map { CatalogSearchRules.normalize(array.getString(it)) }.filter(CatalogSearchRules::valid).distinctBy(CatalogSearchRules::key).take(CatalogSearchRules.HISTORY_SIZE)
    }.getOrDefault(emptyList()))
    val queries = mutable.asStateFlow()
    suspend fun remember(value: String) = update { current ->
        val query = CatalogSearchRules.normalize(value)
        if (CatalogSearchRules.valid(query)) (listOf(query) + current.filterNot { CatalogSearchRules.key(it) == CatalogSearchRules.key(query) }).take(CatalogSearchRules.HISTORY_SIZE) else current
    }
    suspend fun remove(value: String) = update { it.filterNot { query -> CatalogSearchRules.key(query) == CatalogSearchRules.key(value) } }
    suspend fun clear() = update { emptyList() }
    private suspend fun update(transform: (List<String>) -> List<String>): CatalogResult<Unit> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val next = transform(mutable.value)
            if (preferences.edit().putString(key, JSONArray(next).toString()).commit()) {
                mutable.value = next
                CatalogResult.Success(Unit)
            } else CatalogResult.Failure(CatalogError.Storage)
        }
    }
}
