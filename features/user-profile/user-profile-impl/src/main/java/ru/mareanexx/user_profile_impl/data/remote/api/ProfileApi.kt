package ru.mareanexx.user_profile_impl.data.remote.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.mareanexx.user_profile_impl.data.remote.dto.*

internal interface ProfileApi {
    @GET("api/v1/users/me")
    suspend fun getProfile(): Response<ProfileDto>

    @PATCH("api/v1/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ProfileDto>

    @Multipart
    @PUT("api/v1/users/me/avatar")
    suspend fun updateAvatar(@Part image: MultipartBody.Part): Response<ProfileDto>

    @DELETE("api/v1/users/me/avatar")
    suspend fun removeAvatar(): Response<ProfileDto>

    @GET("api/v1/users/me/achievements")
    suspend fun getAchievements(): Response<AchievementsResponse>

    @GET("api/v1/users/me/settings")
    suspend fun getSettings(): Response<SettingsDto>

    @PATCH("api/v1/users/me/settings")
    suspend fun updateSettings(@Body settings: SettingsDto): Response<SettingsDto>
}
