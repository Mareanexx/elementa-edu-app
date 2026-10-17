package ru.mareanexx.user_profile_impl

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_api.domain.repository.ProfileRepository

internal class FakeProfileRepository : ProfileRepository {
    override val settings = MutableStateFlow(AppSettings())
    var profile = UserProfile("1", "Мария Химик", "chemist", "Привет", null, ProfileStatistics(24, 41, 8))
    var failure: ProfileFailure? = null
    var gate: CompletableDeferred<Unit>? = null
    var achievementGate: CompletableDeferred<Unit>? = null
    var achievementFailure: ProfileFailure? = null
    var achievements = emptyList<Achievement>()
    var achievementCalls = 0
    var profileCalls = 0
    var settingsCalls = 0
    var avatarCalls = 0
    override suspend fun getProfile() = ProfileResult.Success(profile)
    override suspend fun getAchievements(): ProfileResult<List<Achievement>> {
        achievementCalls++
        achievementGate?.await()
        return achievementFailure?.let { ProfileResult.Failure(it) } ?: ProfileResult.Success(achievements)
    }
    override suspend fun getSettings() = ProfileResult.Success(settings.value)
    override suspend fun clearImageCache() = ProfileResult.Success(Unit)
    override suspend fun updateProfile(form: ProfileEditForm): ProfileResult<UserProfile> {
        profileCalls++
        gate?.await()
        failure?.let { return ProfileResult.Failure(it) }
        profile = profile.copy(fullName = form.fullName, username = form.username, description = form.description)
        return ProfileResult.Success(profile)
    }
    override suspend fun updateAvatar(image: AvatarUpload): ProfileResult<UserProfile> {
        avatarCalls++
        gate?.await()
        return ProfileResult.Success(profile.copy(avatarUrl = "https://example.com/avatar.jpg"))
    }
    override suspend fun removeAvatar(): ProfileResult<UserProfile> { avatarCalls++; return ProfileResult.Success(profile.copy(avatarUrl = null)) }
    override suspend fun updateSettings(settings: AppSettings): ProfileResult<AppSettings> {
        settingsCalls++
        gate?.await()
        failure?.let { return ProfileResult.Failure(it) }
        this.settings.value = settings
        return ProfileResult.Success(settings)
    }
}
