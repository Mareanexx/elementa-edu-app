package ru.mareanexx.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import ru.mareanexx.datastore.crypto.SessionCipher

private val Context.sessionDataStore by preferencesDataStore(name = "auth_session")

/** Читает и записывает зашифрованную строку; формат сессии определяет вызывающий модуль. */
class SecureSessionStore(
    context: Context,
    private val cipher: SessionCipher,
) : SessionStore {
    private val store = context.applicationContext.sessionDataStore
    private val keyName = stringPreferencesKey("encrypted_session")

    override val data = store.data.map { preferences -> preferences[keyName]?.let(cipher::decrypt) }

    override suspend fun save(value: String) {
        val encrypted = cipher.encrypt(value)
        store.edit { it[keyName] = encrypted }
    }

    override suspend fun clear() { store.edit { it.remove(keyName) } }
}
