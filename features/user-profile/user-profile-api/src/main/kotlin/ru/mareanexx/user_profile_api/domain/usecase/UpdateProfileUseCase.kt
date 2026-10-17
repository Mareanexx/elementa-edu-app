package ru.mareanexx.user_profile_api.domain.usecase

import java.util.Locale
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_api.domain.repository.ProfileRepository
import ru.mareanexx.user_profile_api.domain.validation.ProfileValidator

class UpdateProfileUseCase(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(form: ProfileEditForm): ProfileResult<UserProfile> {
        val errors = ProfileValidator.validate(form)
        if (errors.isNotEmpty()) return ProfileResult.Failure(ProfileFailure(ProfileError.InvalidField, errors))
        return repository.updateProfile(form.copy(
            fullName = form.fullName.trim(),
            username = form.username.trim().lowercase(Locale.ROOT),
            description = form.description.trim(),
        ))
    }
}
