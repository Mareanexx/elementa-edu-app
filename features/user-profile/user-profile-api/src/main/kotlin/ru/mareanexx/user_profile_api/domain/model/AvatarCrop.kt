package ru.mareanexx.user_profile_api.domain.model

data class AvatarCrop(
    val zoom: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
) {
    fun clamp(width: Int, height: Int): AvatarCrop {
        val base = maxOf(1f / width, 1f / height) * zoom
        val maxX = ((width * base - 1) / 2).coerceAtLeast(0f)
        val maxY = ((height * base - 1) / 2).coerceAtLeast(0f)
        return copy(offsetX = offsetX.coerceIn(-maxX, maxX), offsetY = offsetY.coerceIn(-maxY, maxY))
    }
}
