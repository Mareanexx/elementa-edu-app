package ru.mareanex.auth_impl.data.mock

import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonObject
import ru.mareanex.auth_impl.data.remote.dto.AuthUserDto
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Хранит демо-аккаунты с солью и хешем пароля. В release-сборку не входит. */
internal class DemoAccountStore(
    context: Context,
) {
    private val preferences = context.applicationContext.getSharedPreferences("auth_demo_accounts", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun register(fullName: String, username: String, password: String): AuthUserDto? = synchronized(preferences) {
        ensureSeed()
        if (username == "taken_user" || preferences.contains(username)) return null
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        val id = UUID.randomUUID().toString()
        val record = mapOf("id" to id, "full_name" to fullName, "salt" to encode(salt), "hash" to encode(hash(password, salt)))
        if (!preferences.edit().putString(username, gson.toJson(record)).commit()) throw IOException("Не удалось сохранить демо-аккаунт")
        AuthUserDto(id, username, fullName)
    }

    fun login(username: String, password: String): AuthUserDto? = synchronized(preferences) {
        ensureSeed()
        val record = preferences.getString(username, null)?.let { gson.fromJson(it, JsonObject::class.java) } ?: return null
        val matches = MessageDigest.isEqual(
            hash(password, Base64.decode(record.get("salt").asString, Base64.NO_WRAP)),
            Base64.decode(record.get("hash").asString, Base64.NO_WRAP))
        if (matches) AuthUserDto(record.get("id").asString, username, record.get("full_name").asString) else null
    }

    fun update(id: String, fullName: String, username: String): Boolean = synchronized(preferences) {
        ensureSeed()
        if (username == "taken_user") return false
        val entries = preferences.all.mapNotNull { (key, value) ->
            if (value !is String) null else runCatching { key to gson.fromJson(value, JsonObject::class.java) }.getOrNull()
        }
        if (entries.any { (key, value) -> key == username && value.get("id").asString != id }) return false
        val entry = entries.firstOrNull { (_, value) -> value.get("id").asString == id } ?: return false
        entry.second.addProperty("full_name", fullName)
        if (!preferences.edit().remove(entry.first).putString(username, gson.toJson(entry.second)).commit()) {
            throw IOException("Не удалось изменить демо-аккаунт")
        }
        true
    }

    private fun ensureSeed() {
        if (preferences.getBoolean("__seeded", false)) return
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        val record = mapOf(
            "id" to "demo-chemist", "full_name" to "Химик Elementa",
            "salt" to encode(salt), "hash" to encode(hash("Elementa123", salt)),
        )
        if (!preferences.edit().putString("chemist", gson.toJson(record)).putBoolean("__seeded", true).commit()) {
            throw IOException("Не удалось подготовить демо-аккаунт")
        }
    }

    private fun hash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }

    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)
}
