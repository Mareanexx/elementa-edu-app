package ru.mareanexx.ui.modifiers

import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Стандартная длительность throttle для клика
 */
private val THROTTLE_DURATION = 300.milliseconds

/**
 * [Modifier] с реализацией [clickable] и поддержкой throttle.
 */
@Composable
fun Modifier.elementaClickable(
    role: Role? = Role.Button,
    label: String? = null,
    interactionSource: MutableInteractionSource? = null,
    isEnabled: Boolean = true,
    isRippleEnabled: Boolean = true,
    isThrottleEnabled: Boolean = true,
    onClick: () -> Unit,
): Modifier {
    val throttle = if (isThrottleEnabled) THROTTLE_DURATION else Duration.ZERO
    val indication = remember(isRippleEnabled) {
        if (isRippleEnabled) ripple() else null
    }

    val lastClickTime = remember { mutableLongStateOf(0L) }
    val clickHandler = remember(lastClickTime, throttle, onClick) {
        createThrottleCallback(lastClickTime, throttle, onClick)
    }

    return this.clickable(
        enabled = isEnabled,
        onClickLabel = label,
        role = role,
        interactionSource = interactionSource,
        indication = indication,
        onClick = clickHandler,
    )
}

/**
 * Утилита для генерации callback обработчиков кликов с учетом throttle
 */
private fun createThrottleCallback(
    lastClickTime: MutableLongState,
    throttle: Duration,
    callback: () -> Unit,
): () -> Unit {
    return {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime.value >= throttle.inWholeMilliseconds) {
            lastClickTime.value = currentTime
            callback.invoke()
        }
    }
}
