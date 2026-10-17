package ru.mareanexx.collections_impl.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import ru.mareanexx.collections_api.domain.model.*

internal class CollectionsPreferencesSource(
    context: Context,
    userId: String,
) {
    private val store = context.getSharedPreferences("collections_preferences", Context.MODE_PRIVATE)
    private val key = "preferences_$userId"
    private fun read(): CollectionsPreferences {
        val parts = store.getString(key, null)?.split('|')
        val sort = CollectionSort.entries.firstOrNull { it.name == parts?.firstOrNull() } ?: CollectionSort.NameAscending
        return CollectionsPreferences(sort, parts?.getOrNull(1) != "false")
    }
    val flow = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changed -> if (changed == key) trySend(read()) }
        store.registerOnSharedPreferenceChangeListener(listener)
        trySend(read())
        awaitClose { store.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()
    suspend fun save(value: CollectionsPreferences): CollectionsResult<Unit> = withContext(Dispatchers.IO) {
        try {
            if (store.edit().putString(key, "${value.sort.name}|${value.showEmpty}").commit()) CollectionsResult.Success(Unit)
            else CollectionsResult.Failure(CollectionsError.Storage)
        } catch (_: RuntimeException) { CollectionsResult.Failure(CollectionsError.Storage) }
    }
}
