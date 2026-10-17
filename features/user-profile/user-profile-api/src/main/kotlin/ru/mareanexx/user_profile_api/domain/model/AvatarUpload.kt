package ru.mareanexx.user_profile_api.domain.model

class AvatarUpload(
    val bytes: ByteArray,
    val mimeType: String = "image/jpeg",
    val fileName: String = "avatar.jpg",
)
