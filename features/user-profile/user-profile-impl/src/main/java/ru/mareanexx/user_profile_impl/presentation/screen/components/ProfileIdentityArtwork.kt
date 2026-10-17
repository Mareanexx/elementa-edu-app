package ru.mareanexx.user_profile_impl.presentation.screen.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/** Спокойный молекулярный узор объединяет карточку профиля с химической темой приложения. */
@Composable
internal fun ProfileIdentityArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val center = Offset(size.width * .82f, size.height * .1f)
        val radius = size.height * .7f
        drawCircle(Color.White.copy(alpha = .035f), radius * 1.35f, center)
        drawCircle(Color.White.copy(alpha = .07f), radius, center, style = Stroke(1.dp.toPx()))
        val nodes = (0..5).map { index ->
            val angle = Math.PI / 3 * index
            center + Offset(cos(angle).toFloat() * radius * .65f, sin(angle).toFloat() * radius * .65f)
        }
        nodes.forEachIndexed { index, node ->
            drawLine(Color.White.copy(alpha = .12f), node, nodes[(index + 1) % nodes.size], 1.dp.toPx())
            drawCircle(Color.White.copy(alpha = .18f), 3.dp.toPx(), node)
        }
    }
}
