package ru.mareanexx.user_profile_impl.presentation.profile

import ru.mareanexx.user_profile_api.domain.model.*

data class ProfileUiState(
    val profile: UserProfile? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val avatarBusy: Boolean = false,
    val failure: ProfileFailure? = null,
    val form: ProfileEditForm = ProfileEditForm(),
    val errors: Map<ProfileField, ProfileError> = emptyMap(),
    val touched: Set<ProfileField> = emptySet(),
) {
    val dirty: Boolean get() = profile?.let {
        form != ProfileEditForm(it.fullName, it.username, it.description)
    } ?: false
}
