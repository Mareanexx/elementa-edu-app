package ru.mareanexx.user_profile_api.domain.model

data class AppSettings(
    val textScale: Float = 1f,
    val reduceMotion: Boolean = false,
    val keepScreenOn: Boolean = false,
    val hapticFeedback: Boolean = true,
)
