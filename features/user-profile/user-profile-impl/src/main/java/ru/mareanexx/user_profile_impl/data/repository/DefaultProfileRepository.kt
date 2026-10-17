package ru.mareanexx.user_profile_impl.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_api.domain.repository.ProfileRepository
import ru.mareanexx.user_profile_impl.data.local.ProfileImageCache
import ru.mareanexx.user_profile_impl.data.local.SettingsLocalDataSource
import ru.mareanexx.user_profile_impl.data.remote.source.ProfileRemoteDataSource

internal class DefaultProfileRepository(
    private val remote: ProfileRemoteDataSource,
    private val local: SettingsLocalDataSource,
    private val cache: ProfileImageCache,
) : ProfileRepository {
    override val settings = local.settings
    override suspend fun getProfile() = remote.getProfile()
    override suspend fun updateProfile(form: ProfileEditForm) = remote.updateProfile(form)
    override suspend fun updateAvatar(image: AvatarUpload) = remote.updateAvatar(image)
    override suspend fun removeAvatar() = remote.removeAvatar()
    override suspend fun getAchievements() = remote.getAchievements()
    override suspend fun getSettings() = persist(remote.getSettings())
    override suspend fun updateSettings(settings: AppSettings) = persist(remote.updateSettings(settings))
    override suspend fun clearImageCache(): ProfileResult<Unit> = localOperation { cache.clear() }

    private suspend fun persist(result: ProfileResult<AppSettings>): ProfileResult<AppSettings> = when (result) {
        is ProfileResult.Failure -> result
        is ProfileResult.Success -> when (val saved = localOperation { local.save(result.value) }) {
            is ProfileResult.Failure -> saved
            is ProfileResult.Success -> result
        }
    }

    private suspend fun <T> localOperation(block: () -> T): ProfileResult<T> = withContext(Dispatchers.IO) {
        try {
            ProfileResult.Success(block())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            ProfileResult.Failure(ProfileFailure(ProfileError.Storage))
        }
    }
}
