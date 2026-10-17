package ru.mareanexx.user_profile_api.domain.validation

import ru.mareanexx.user_profile_api.domain.model.*

object ProfileValidator {
    const val MAX_DESCRIPTION_LENGTH = 250

    fun validate(form: ProfileEditForm): Map<ProfileField, ProfileError> = buildMap {
        val name = form.fullName.trim()
        if (name.isEmpty()) put(ProfileField.FullName, ProfileError.Required)
        else if (name.length !in 2..80 || name.none(Char::isLetter)) put(ProfileField.FullName, ProfileError.InvalidName)
        val username = form.username.trim()
        if (username.isEmpty()) put(ProfileField.Username, ProfileError.Required)
        else if (!Regex("[a-zA-Z][a-zA-Z0-9_]{2,31}").matches(username)) {
            put(ProfileField.Username, ProfileError.InvalidUsername)
        }
        if (form.description.length > MAX_DESCRIPTION_LENGTH) put(ProfileField.Description, ProfileError.DescriptionTooLong)
    }
}
