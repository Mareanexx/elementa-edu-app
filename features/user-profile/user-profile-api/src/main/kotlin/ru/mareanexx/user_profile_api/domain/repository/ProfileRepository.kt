package ru.mareanexx.user_profile_api.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mareanexx.user_profile_api.domain.model.*

interface ProfileRepository {
    val settings: Flow<AppSettings>
    suspend fun getProfile(): ProfileResult<UserProfile>
    suspend fun updateProfile(form: ProfileEditForm): ProfileResult<UserProfile>
    suspend fun updateAvatar(image: AvatarUpload): ProfileResult<UserProfile>
    suspend fun removeAvatar(): ProfileResult<UserProfile>
    suspend fun getAchievements(): ProfileResult<List<Achievement>>
    suspend fun getSettings(): ProfileResult<AppSettings>
    suspend fun updateSettings(settings: AppSettings): ProfileResult<AppSettings>
    suspend fun clearImageCache(): ProfileResult<Unit>
}
