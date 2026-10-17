package ru.mareanexx.user_profile_impl.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import ru.mareanexx.user_profile_api.domain.model.AppSettings
import java.io.IOException

/** Настройки разделены по пользователям; токены и пароли здесь не хранятся. */
internal class SettingsLocalDataSource(
    context: Context,
    userId: String,
    private val gson: Gson,
) {
    private val preferences = context.applicationContext.getSharedPreferences("elementa_settings", Context.MODE_PRIVATE)
    private val key = "settings_$userId"
    val settings = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changed ->
            if (changed == key) trySend(read())
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(read())
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    private fun read(): AppSettings = runCatching {
        val settings = gson.fromJson(preferences.getString(key, null), AppSettings::class.java) ?: AppSettings()
        settings.copy(textScale = settings.textScale.takeIf { it.isFinite() && it in 0.9f..1.3f } ?: 1f)
    }.getOrDefault(AppSettings())

    fun save(settings: AppSettings) {
        if (!preferences.edit().putString(key, gson.toJson(settings)).commit()) {
            throw IOException("Не удалось сохранить настройки")
        }
    }
}
