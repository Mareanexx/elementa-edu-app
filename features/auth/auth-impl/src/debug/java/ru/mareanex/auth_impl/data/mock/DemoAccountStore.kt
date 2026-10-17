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
        if (username == "chemist" || username == "taken_user" || preferences.contains(username)) return null
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        val id = UUID.randomUUID().toString()
        val record = mapOf("id" to id, "full_name" to fullName, "salt" to encode(salt), "hash" to encode(hash(password, salt)))
        if (!preferences.edit().putString(username, gson.toJson(record)).commit()) throw IOException("Не удалось сохранить демо-аккаунт")
        AuthUserDto(id, username, fullName)
    }

    fun login(username: String, password: String): AuthUserDto? = synchronized(preferences) {
        if (username == "chemist" && password == "Elementa123") return AuthUserDto("demo-chemist", username, "Химик Elementa")
        val record = preferences.getString(username, null)?.let { gson.fromJson(it, JsonObject::class.java) } ?: return null
        val matches = MessageDigest.isEqual(
            hash(password, Base64.decode(record.get("salt").asString, Base64.NO_WRAP)),
            Base64.decode(record.get("hash").asString, Base64.NO_WRAP))
        if (matches) AuthUserDto(record.get("id").asString, username, record.get("full_name").asString) else null
    }

    private fun hash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }

    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)
}
