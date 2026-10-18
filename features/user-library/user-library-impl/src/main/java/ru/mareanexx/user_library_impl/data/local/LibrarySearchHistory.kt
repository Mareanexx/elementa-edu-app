package ru.mareanexx.user_library_impl.data.local

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_api.domain.validation.LibrarySearchRules

internal class LibrarySearchHistory(
    context: Context,
    userId: String,
) {
    private val preferences = context.getSharedPreferences("library_search_history", Context.MODE_PRIVATE)
    private val key = "queries_$userId"
    private val gson = Gson()
    private val mutable = MutableStateFlow(runCatching {
        gson.fromJson(preferences.getString(key, "[]"), Array<String>::class.java).toList()
    }.getOrDefault(emptyList()))
    val queries = mutable.asStateFlow()
    suspend fun remember(value: String): LibraryResult<Unit> {
        val query = LibrarySearchRules.normalize(value)
        if (query.length < LibrarySearchRules.MIN_LENGTH) return LibraryResult.Success(Unit)
        return save((listOf(query) + mutable.value.filterNot { it.equals(query, true) }).take(LibrarySearchRules.HISTORY_LIMIT))
    }
    suspend fun clear() = save(emptyList())
    private suspend fun save(items: List<String>): LibraryResult<Unit> = withContext(Dispatchers.IO) {
        if (preferences.edit().putString(key, gson.toJson(items)).commit()) {
            mutable.value = items
            LibraryResult.Success(Unit)
        } else LibraryResult.Failure(LibraryError.Storage)
    }
}
