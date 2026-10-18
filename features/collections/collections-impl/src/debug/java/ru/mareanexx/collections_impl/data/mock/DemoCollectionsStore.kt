package ru.mareanexx.collections_impl.data.mock

import android.content.Context
import com.google.gson.Gson
import java.io.IOException

internal class DemoCollectionsStore(
    context: Context,
    private val userId: String,
) {
    private val store = context.getSharedPreferences("collections_demo_server", Context.MODE_PRIVATE)
    private val key = "collections_$userId"
    private val gson = Gson()
    private val preferencesKey = "preferences_$userId"
    fun preferences(): ru.mareanexx.collections_impl.data.remote.dto.CollectionPreferencesDto =
        store.getString(preferencesKey, null)?.let { gson.fromJson(it, ru.mareanexx.collections_impl.data.remote.dto.CollectionPreferencesDto::class.java) }
            ?: ru.mareanexx.collections_impl.data.remote.dto.CollectionPreferencesDto("name_asc", true)
    fun savePreferences(value: ru.mareanexx.collections_impl.data.remote.dto.CollectionPreferencesDto) {
        if (!store.edit().putString(preferencesKey, gson.toJson(value)).commit()) throw IOException("Не удалось сохранить настройки коллекций")
    }
    fun read(): List<DemoStoredCollection> {
        val json = store.getString(key, null)
        if (json != null) {
            val records = requireNotNull(gson.fromJson(json, Array<DemoStoredCollection>::class.java)).toList()
            return withReadingList(records)
        }
        val demo = userId == "demo-chemist"
        val favorites = DemoStoredCollection("favorites", "Избранное", "chemistry", true, if (demo) listOf("book_1", "book_2", "book_4", "book_7") else emptyList())
        val custom = if (!demo) emptyList() else listOf(
            DemoStoredCollection("organic", "Органическая химия", "chemistry", false, listOf("book_1", "book_8", "book_11")),
            DemoStoredCollection("science", "Научные статьи", "molecules", false, listOf("book_5", "book_12")),
            DemoStoredCollection("quantum", "Квантовая химия", "space", false, listOf("book_3")),
            DemoStoredCollection("biochemistry", "Биохимия", "laboratory", false, listOf("book_2", "book_10")),
            DemoStoredCollection("exam", "Подготовка к экзамену", "notes", false, emptyList()),
            DemoStoredCollection("discoveries", "Мои открытия", "reactions", false, listOf("book_9")),
        )
        return withReadingList(listOf(favorites) + custom)
    }
    private fun withReadingList(items: List<DemoStoredCollection>): List<DemoStoredCollection> {
        if (items.any { it.readingList }) return items
        return (items + DemoStoredCollection("reading-list", "Хочу прочитать", "notes", false, emptyList(), readingList = true)).also(::save)
    }
    fun save(items: List<DemoStoredCollection>) {
        if (!store.edit().putString(key, gson.toJson(items)).commit()) throw IOException("Не удалось сохранить демонстрационные коллекции")
    }
    companion object { val lock = Any() }
}
