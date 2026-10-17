package ru.mareanexx.user_profile_api.domain.usecase

import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_api.domain.repository.ProfileRepository

class UpdateAvatarUseCase(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(image: AvatarUpload): ProfileResult<UserProfile> {
        if (image.bytes.isEmpty() || image.mimeType !in setOf("image/jpeg", "image/png")) {
            return ProfileResult.Failure(ProfileFailure(ProfileError.InvalidImage))
        }
        if (image.bytes.size > 5 * 1024 * 1024) {
            return ProfileResult.Failure(ProfileFailure(ProfileError.TooLarge))
        }
        return repository.updateAvatar(image)
    }
}
