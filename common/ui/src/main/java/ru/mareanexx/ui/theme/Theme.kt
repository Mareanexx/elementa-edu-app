package ru.mareanexx.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ElementaAppTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        typography = ElementaTypography,
        content = content,
    )
}