package ru.mareanexx.user_profile_api.domain.model

enum class ProfileError {
    Required, InvalidField, InvalidName, InvalidUsername, DescriptionTooLong, UsernameTaken,
    Offline, Timeout, Unauthorized, Unavailable, InvalidResponse, Storage, InvalidImage, TooLarge, RateLimited,
}
