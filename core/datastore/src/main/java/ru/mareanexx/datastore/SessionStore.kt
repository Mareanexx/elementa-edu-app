package ru.mareanexx.datastore

import kotlinx.coroutines.flow.Flow

/** Контракт защищённого хранилища, не зависящий от формата данных авторизации. */
interface SessionStore {
    val data: Flow<String?>
    suspend fun save(value: String)
    suspend fun clear()
}
