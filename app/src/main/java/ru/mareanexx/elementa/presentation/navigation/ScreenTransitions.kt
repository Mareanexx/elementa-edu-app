package ru.mareanexx.elementa.presentation.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween

private const val TRANSITION_DURATION_MILLIS = 360
private val NavigationEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

// Передний экран проходит полную ширину, а нижний смещается с параллаксом.
internal fun pushEnter() = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION_MILLIS, easing = NavigationEasing),
    initialOffsetX = { it },
)

internal fun pushExit() = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION_MILLIS, easing = NavigationEasing),
    targetOffsetX = { -it / 3 },
)

internal fun popEnter() = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION_MILLIS, easing = NavigationEasing),
    initialOffsetX = { -it / 3 },
)

internal fun popExit() = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION_MILLIS, easing = NavigationEasing),
    targetOffsetX = { it },
)
