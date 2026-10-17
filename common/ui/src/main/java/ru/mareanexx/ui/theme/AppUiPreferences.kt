package ru.mareanexx.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

data class AppUiPreferences(
    val reduceMotion: Boolean = false,
    val hapticFeedback: Boolean = true,
)

val LocalAppUiPreferences = staticCompositionLocalOf { AppUiPreferences() }
