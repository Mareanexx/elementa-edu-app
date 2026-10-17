package ru.mareanex.auth_impl.data.mock

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.UUID
import ru.mareanex.auth_impl.data.remote.dto.AuthResponse

/** Подменяет HTTP-ответы только в debug-сборке; сериализация Retrofit остаётся настоящей. */
internal class DemoAuthInterceptor(
    context: Context,
) : Interceptor {
    private val accounts = DemoAccountStore(context)
    private val gson = Gson()
    private val limitedUsers = mutableSetOf<String>()

    override fun intercept(chain: Interceptor.Chain): Response {
        Thread.sleep(1_200)
        val request = chain.request()
        val buffer = Buffer()
        request.body?.writeTo(buffer)
        val json = gson.fromJson(buffer.readUtf8(), JsonObject::class.java)
        val username = json.get("username").asString
        val password = json.get("password").asString
        fun response(status: Int, body: Any, retry: String? = null): Response = Response.Builder()
            .request(request).protocol(Protocol.HTTP_1_1).code(status).message("Demo response")
            .body(gson.toJson(body).toResponseBody("application/json".toMediaType()))
            .apply { if (retry != null) header("Retry-After", retry) }.build()
        fun error(status: Int, code: String, fields: Map<String, String> = emptyMap(), retry: String? = null) =
            response(status, mapOf("error" to mapOf("code" to code, "field_errors" to fields)), retry)

        when (username) {
            "offline_user" -> throw IOException("Имитация отсутствия сети")
            "timeout_user" -> throw SocketTimeoutException("Имитация тайм-аута")
            "server_error" -> return error(503, "SERVICE_UNAVAILABLE")
            "blocked_user" -> return error(403, "ACCOUNT_DISABLED")
            "invalid_response" -> return response(200, mapOf("unexpected" to true))
            "validation_error" -> return error(422, "VALIDATION_ERROR", mapOf("username" to "INVALID_VALUE"))
            "rate_limit" -> synchronized(limitedUsers) {
                if (limitedUsers.add(username)) return error(429, "RATE_LIMITED", retry = "10")
            }
        }
        if (request.url.encodedPath !in setOf("/api/v1/auth/login", "/api/v1/auth/register")) {
            return error(404, "NOT_FOUND")
        }
        val registering = request.url.encodedPath.endsWith("register")
        val user = if (registering) {
            try {
                accounts.register(json.get("full_name").asString, username, password)
                    ?: return error(409, "USERNAME_TAKEN", mapOf("username" to "USERNAME_TAKEN"))
            } catch (_: IOException) { return error(503, "SERVICE_UNAVAILABLE") }
        } else accounts.login(username, password) ?: return error(401, "INVALID_CREDENTIALS")
        return response(if (registering) 201 else 200, AuthResponse(
            "demo-access-${UUID.randomUUID()}", "demo-refresh-${UUID.randomUUID()}",
            86_400, "Bearer", user))
    }
}
