package ru.mareanexx.user_profile_impl.data.mock

import android.content.Context
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_api.domain.validation.ProfileValidator
import ru.mareanexx.user_profile_impl.data.remote.dto.*
import ru.mareanexx.user_profile_impl.data.remote.mapper.SettingsMapper

internal class DemoProfileInterceptor(
    context: Context,
    private val userId: String,
    name: String,
    username: String,
    private val updateAccount: (String, String, String) -> Boolean,
) : Interceptor {
    private val store = DemoProfileStore(context.applicationContext, userId, name, username)
    private val gson = Gson()
    private val controls = context.getSharedPreferences("profile_mock_controls", Context.MODE_PRIVATE)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        if (!path.startsWith("/api/v1/users/me")) return chain.proceed(request)
        Thread.sleep(450)
        if (request.header("Authorization").isNullOrBlank()) return error(request, 401, "UNAUTHORIZED")
        val status = controls.getInt("next_status", 0)
        if (status != 0) {
            controls.edit().remove("next_status").commit()
            if (status == -1) throw java.io.IOException("Демо: отсутствует подключение")
            return error(request, status, "DEMO_ERROR")
        }
        return try {
            when {
                path.endsWith("/achievements") && request.method == "GET" -> ok(request, AchievementsResponse(DemoAchievements.create(requireNotNull(store.profile().statistics), userId == "demo-chemist")))
                path.endsWith("/settings") && request.method == "GET" -> ok(request, store.settings())
                path.endsWith("/settings") && request.method == "PATCH" -> {
                    val settings = gson.fromJson(body(request), SettingsDto::class.java)
                    SettingsMapper.map(settings)
                    ok(request, store.save(settings))
                }
                path.endsWith("/avatar") && request.method == "DELETE" -> ok(request, store.removeAvatar())
                path.endsWith("/avatar") && request.method == "PUT" -> upload(request)
                path == "/api/v1/users/me" && request.method == "GET" -> ok(request, store.profile())
                path == "/api/v1/users/me" && request.method == "PATCH" -> update(request)
                else -> error(request, 404, "NOT_FOUND")
            }
        } catch (_: java.io.IOException) {
            error(request, 503, "SERVICE_UNAVAILABLE")
        } catch (_: RuntimeException) {
            error(request, 422, "INVALID_REQUEST")
        }
    }

    private fun update(request: Request): Response {
        val form = gson.fromJson(body(request), UpdateProfileRequest::class.java)
        val validation = ProfileValidator.validate(ProfileEditForm(form.fullName, form.username, form.description))
        if (validation.isNotEmpty()) {
            val fields = validation.mapKeys { (key, _) -> when (key) {
                ProfileField.FullName -> "full_name"
                ProfileField.Username -> "username"
                ProfileField.Description -> "description"
            } }.mapValues { (_, value) -> when (value) {
                ProfileError.InvalidName -> "INVALID_NAME"
                ProfileError.InvalidUsername -> "INVALID_USERNAME"
                ProfileError.DescriptionTooLong -> "DESCRIPTION_TOO_LONG"
                else -> "REQUIRED"
            } }
            return response(request, 422, mapOf("error" to mapOf("code" to "VALIDATION_ERROR", "field_errors" to fields)))
        }
        if (!updateAccount(userId, form.fullName, form.username)) return error(request, 409, "USERNAME_TAKEN")
        return ok(request, store.save(store.profile().copy(fullName = form.fullName, username = form.username, description = form.description)))
    }

    private fun upload(request: Request): Response {
        val multipart = request.body as? MultipartBody ?: return error(request, 422, "INVALID_REQUEST")
        val part = multipart.parts.firstOrNull { it.headers?.get("Content-Disposition")?.contains("name=\"file\"") == true }
            ?: return error(request, 422, "INVALID_REQUEST")
        val bytes = Buffer().also { part.body.writeTo(it) }.readByteArray()
        if (bytes.size > 5 * 1024 * 1024) return error(request, 413, "FILE_TOO_LARGE")
        if (bytes.isEmpty() || part.body.contentType()?.toString() !in listOf("image/jpeg", "image/png")) return error(request, 415, "INVALID_IMAGE")
        return ok(request, store.avatar(bytes))
    }

    private fun body(request: Request) = Buffer().also { request.body?.writeTo(it) }.readUtf8()
    private fun ok(request: Request, data: Any) = response(request, 200, data)
    private fun error(request: Request, code: Int, kind: String) = response(request, code, mapOf("error" to mapOf("code" to kind)))
    private fun response(request: Request, code: Int, data: Any) = Response.Builder()
        .request(request).protocol(Protocol.HTTP_1_1).code(code).message("Демо-профиль")
        .body(gson.toJson(data).toResponseBody("application/json".toMediaType())).build()
}
