package ru.mareanexx.user_profile_impl.data.mock

import android.content.Context
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.util.UUID
import ru.mareanexx.user_profile_impl.data.remote.dto.*

/** Имитирует серверное хранилище только в debug; данные изолированы по id пользователя. */
internal class DemoProfileStore(
    private val context: Context,
    private val userId: String,
    private val initialName: String,
    private val initialUsername: String,
) {
    private val preferences = context.getSharedPreferences("profile_demo_server", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val profileKey = "profile_$userId"
    private val settingsKey = "settings_$userId"

    fun profile(): ProfileDto = preferences.getString(profileKey, null)?.let {
        gson.fromJson(it, ProfileDto::class.java)
    } ?: ProfileDto(
        userId, initialName, initialUsername,
        if (userId == "demo-chemist") "Люблю учить химию через литературу и исследования" else "",
        null, if (userId == "demo-chemist") StatisticsDto(24, 41, 8) else StatisticsDto(0, 0, 0),
    )

    fun save(profile: ProfileDto): ProfileDto {
        if (!preferences.edit().putString(profileKey, gson.toJson(profile)).commit()) throw IOException("Не удалось сохранить профиль")
        return profile
    }

    fun settings(): SettingsDto = preferences.getString(settingsKey, null)?.let {
        gson.fromJson(it, SettingsDto::class.java)
    } ?: SettingsDto(1f, false, false, true)

    fun save(settings: SettingsDto): SettingsDto {
        if (!preferences.edit().putString(settingsKey, gson.toJson(settings)).commit()) throw IOException("Не удалось сохранить настройки")
        return settings
    }

    fun avatar(bytes: ByteArray): ProfileDto {
        val folder = File(context.filesDir, "demo_avatars").apply { mkdirs() }
        val file = File(folder, UUID.randomUUID().toString() + ".jpg")
        file.writeBytes(bytes)
        val previous = profile()
        try {
            return save(previous.copy(avatarUrl = file.toURI().toString())).also { removeOldAvatar(previous.avatarUrl) }
        } catch (error: Exception) {
            file.delete()
            throw error
        }
    }

    fun removeAvatar(): ProfileDto {
        val previous = profile()
        return save(previous.copy(avatarUrl = null)).also { removeOldAvatar(previous.avatarUrl) }
    }

    private fun removeOldAvatar(url: String?) {
        if (url == null) return
        runCatching {
            val file = File(java.net.URI(url))
            if (file.canonicalFile.parentFile == File(context.filesDir, "demo_avatars").canonicalFile) file.delete()
        }
    }
}
