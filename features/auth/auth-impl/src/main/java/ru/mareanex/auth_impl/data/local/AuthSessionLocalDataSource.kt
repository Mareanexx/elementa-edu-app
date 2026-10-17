package ru.mareanex.auth_impl.data.local

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.mareanex.auth_impl.data.model.AuthSessionData
import ru.mareanexx.datastore.SessionStore

/** Отвечает за формат хранения и проверку сохранённых данных, не зная об HTTP. */
internal class AuthSessionLocalDataSource(
    private val store: SessionStore,
    private val gson: Gson,
    private val now: () -> Long = System::currentTimeMillis,
) {
    val session = store.data.map { json ->
        try {
            json?.let { gson.fromJson(it, AuthSessionData::class.java) }
                ?.takeIf { !it.accessToken.isNullOrBlank() && !it.refreshToken.isNullOrBlank() }
                ?.session?.takeIf {
                    it.expiresAtMillis > now() && it.user.id.isNotBlank() && it.user.username.isNotBlank()
                }
        } catch (_: RuntimeException) { null }
    }.flowOn(Dispatchers.IO)

    suspend fun save(session: AuthSessionData) = withContext(Dispatchers.IO) {
        store.save(gson.toJson(session))
    }

    suspend fun clear() = store.clear()
}
