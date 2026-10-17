package ru.mareanexx.user_profile_impl.presentation.profile

sealed interface ProfileEffect {
    data object Saved : ProfileEffect
    data object AvatarUpdated : ProfileEffect
}
