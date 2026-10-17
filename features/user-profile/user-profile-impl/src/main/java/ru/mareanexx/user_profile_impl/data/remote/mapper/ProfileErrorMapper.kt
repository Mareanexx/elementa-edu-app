package ru.mareanexx.user_profile_impl.data.remote.mapper

import com.google.gson.Gson
import retrofit2.Response
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_impl.data.remote.dto.ProfileErrorEnvelope

internal class ProfileErrorMapper(
    private val gson: Gson,
) {
    fun map(response: Response<*>): ProfileFailure {
        val error = runCatching {
            response.errorBody()?.use { gson.fromJson(it.string(), ProfileErrorEnvelope::class.java)?.error }
        }.getOrNull()
        val kind = when (response.code()) {
            401 -> ProfileError.Unauthorized
            409 -> ProfileError.UsernameTaken
            413 -> ProfileError.TooLarge
            415 -> ProfileError.InvalidImage
            408, 504 -> ProfileError.Timeout
            429 -> ProfileError.RateLimited
            400, 422 -> ProfileError.InvalidField
            else -> ProfileError.Unavailable
        }
        val fields = error?.fields.orEmpty().mapNotNull { (key, code) ->
            val field = when (key) {
                "full_name" -> ProfileField.FullName
                "username" -> ProfileField.Username
                "description" -> ProfileField.Description
                else -> null
            }
            field?.let { it to when (code) {
                "USERNAME_TAKEN" -> ProfileError.UsernameTaken
                "INVALID_NAME" -> ProfileError.InvalidName
                "INVALID_USERNAME" -> ProfileError.InvalidUsername
                "DESCRIPTION_TOO_LONG" -> ProfileError.DescriptionTooLong
                else -> ProfileError.Required
            } }
        }.toMap()
        return ProfileFailure(kind, if (kind == ProfileError.UsernameTaken) fields + (ProfileField.Username to kind) else fields)
    }
}
