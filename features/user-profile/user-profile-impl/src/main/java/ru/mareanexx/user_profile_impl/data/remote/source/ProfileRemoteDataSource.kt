package ru.mareanexx.user_profile_impl.data.remote.source

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_impl.data.remote.api.ProfileApi
import ru.mareanexx.user_profile_impl.data.remote.dto.UpdateProfileRequest
import ru.mareanexx.user_profile_impl.data.remote.mapper.*
import com.google.gson.stream.MalformedJsonException
import java.io.EOFException
import java.io.IOException
import java.net.SocketTimeoutException

internal class ProfileRemoteDataSource(
    private val api: ProfileApi,
    private val errors: ProfileErrorMapper,
) {
    suspend fun getProfile() = request({ api.getProfile() }, ProfileMapper::map)
    suspend fun updateProfile(form: ProfileEditForm) = request({
        api.updateProfile(UpdateProfileRequest(form.fullName, form.username, form.description))
    }, ProfileMapper::map)

    suspend fun updateAvatar(image: AvatarUpload) = request({
        api.updateAvatar(MultipartBody.Part.createFormData(
            "file", image.fileName, image.bytes.toRequestBody(image.mimeType.toMediaType()),
        ))
    }, ProfileMapper::map)

    suspend fun removeAvatar() = request({ api.removeAvatar() }, ProfileMapper::map)
    suspend fun getAchievements() = request({ api.getAchievements() }) { response ->
        requireNotNull(response.items).map(AchievementMapper::map).also { items ->
            require(items.map { it.id }.distinct().size == items.size)
        }
    }
    suspend fun getSettings() = request({ api.getSettings() }, SettingsMapper::map)
    suspend fun updateSettings(settings: AppSettings) = request({ api.updateSettings(SettingsMapper.toDto(settings)) }, SettingsMapper::map)

    private suspend fun <T, R> request(call: suspend () -> Response<T>, map: (T) -> R): ProfileResult<R> = withContext(Dispatchers.IO) {
        try {
            val response = call()
            if (!response.isSuccessful) ProfileResult.Failure(errors.map(response))
            else ProfileResult.Success(map(requireNotNull(response.body())))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: SocketTimeoutException) {
            failure(ProfileError.Timeout)
        } catch (_: MalformedJsonException) {
            failure(ProfileError.InvalidResponse)
        } catch (_: EOFException) {
            failure(ProfileError.InvalidResponse)
        } catch (_: IOException) {
            failure(ProfileError.Offline)
        } catch (_: RuntimeException) {
            failure(ProfileError.InvalidResponse)
        }
    }

    private fun failure(error: ProfileError) = ProfileResult.Failure(ProfileFailure(error))
}
