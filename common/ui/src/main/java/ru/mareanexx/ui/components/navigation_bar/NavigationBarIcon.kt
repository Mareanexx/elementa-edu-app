package ru.mareanexx.ui.components.navigation_bar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.painterResource
import ru.mareanexx.ui.theme.bodySecondary

@Composable
internal fun NavigationBarIcon(
    @DrawableRes outline: Int,
    @DrawableRes filled: Int,
    description: String,
    tint: Color,
    fill: Float,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        Icon(painterResource(outline), description, Modifier.fillMaxSize(), tint = tint)
        // Заполненная форма постепенно проявляется снизу вверх внутри того же контура.
        Icon(painterResource(filled), null,
            Modifier.fillMaxSize().drawWithContent {
                clipRect(top = size.height * (1f - fill.coerceIn(0f, 1f))) { this@drawWithContent.drawContent() }
            },
            tint = bodySecondary,
        )
    }
}
