package ru.mareanexx.user_library_impl.data.mock

import android.content.Context
import java.io.IOException

internal class DemoLibraryStore(
    context: Context,
    private val userId: String,
) {
    private val preferences = context.getSharedPreferences("library_demo_server", Context.MODE_PRIVATE)
    fun sort(): String = preferences.getString("sort_$userId", "recent") ?: "recent"
    fun saveSort(sort: String) { if (!preferences.edit().putString("sort_$userId", sort).commit()) throw IOException("Не удалось сохранить сортировку") }
    fun touched(id: String, fallback: Long): Long = preferences.getLong("touched_${userId}_$id", fallback)
    fun touch(id: String) { if (!preferences.edit().putLong("touched_${userId}_$id", System.currentTimeMillis()).commit()) throw IOException("Не удалось сохранить время изменения") }
    companion object { val lock = Any() }
}
