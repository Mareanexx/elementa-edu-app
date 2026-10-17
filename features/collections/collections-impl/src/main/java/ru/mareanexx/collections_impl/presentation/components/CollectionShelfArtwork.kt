package ru.mareanexx.collections_impl.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import ru.mareanexx.collections_api.domain.model.CollectionCover
import kotlin.math.cos
import kotlin.math.sin

/** Матовые книжные обложки масштабируются вместе с карточкой и не требуют загрузки изображений. */
@Composable
internal fun CollectionShelfArtwork(
    cover: CollectionCover,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
) {
    val colors = when (cover) {
        CollectionCover.Chemistry -> listOf(Color(0xFFF5E8DF), Color(0xFF92647F), Color(0xFFD49A7B))
        CollectionCover.Molecules -> listOf(Color(0xFFE3F0EB), Color(0xFF477F80), Color(0xFF9ABDB0))
        CollectionCover.Reactions -> listOf(Color(0xFFF5E5EA), Color(0xFFAA657E), Color(0xFFDFAD9D))
        CollectionCover.Laboratory -> listOf(Color(0xFFE4ECF6), Color(0xFF5979A4), Color(0xFF9FB6D1))
        CollectionCover.Notes -> listOf(Color(0xFFEDE8F6), Color(0xFF8270AF), Color(0xFFB4A6CE))
        CollectionCover.Space -> listOf(Color(0xFFE9E8F3), Color(0xFF55517E), Color(0xFF9B9BBE))
    }
    Canvas(modifier) {
        if (!featured) {
            drawRect(colors[0])
            drawCircle(Color.White.copy(alpha = .35f), size.width * .37f, Offset(size.width * .86f, size.height * .15f))
            drawCircle(colors[1].copy(alpha = .05f), size.width * .32f, Offset(size.width * .08f, size.height))
        }
        val unit = minOf(size.width / 180f, size.height / 120f)
        val center = Offset(size.width * .51f, size.height * .54f)
        val rear = if (featured) Color(0xFFE9BE7E) else colors[2]
        val front = if (featured) Color(0xFFE8E3FC) else colors[1]
        val ink = if (featured) Color(0xFF7060AA) else Color.White.copy(alpha = .85f)
        drawShelfBook(center + Offset(30f * unit, -3f * unit), 57f * unit, 84f * unit, 13f, rear, Color.White.copy(alpha = .7f), cover, false)
        drawShelfBook(center + Offset(-15f * unit, 4f * unit), 64f * unit, 90f * unit, -10f, front, ink, cover, true)
        val ornament = if (featured) Color(0xFFF2CD91) else colors[1].copy(alpha = .45f)
        drawShelfSparkle(center + Offset(-64f * unit, -34f * unit), 5f * unit, ornament)
        drawShelfSparkle(center + Offset(67f * unit, 35f * unit), 3.5f * unit, ornament)
    }
}

private fun DrawScope.drawShelfBook(
    center: Offset,
    width: Float,
    height: Float,
    angle: Float,
    color: Color,
    ink: Color,
    cover: CollectionCover,
    decorated: Boolean,
) {
    rotate(angle, center) {
        val left = center.x - width / 2
        val top = center.y - height / 2
        val radius = CornerRadius(width * .045f)
        drawRoundRect(color.copy(alpha = .55f), Offset(left + width * .04f, top + height * .035f), Size(width, height), radius)
        drawRoundRect(Color(0xFFF9F3E8), Offset(left + width * .04f, top + height * .04f), Size(width * .96f, height * .94f), radius)
        repeat(3) { line ->
            val y = top + height * (.925f + line * .016f)
            drawLine(Color(0xFFD8D0C5), Offset(left + width * .1f, y), Offset(left + width * .98f, y), width * .008f)
        }
        drawRoundRect(color, Offset(left, top), Size(width, height * .92f), radius)
        drawLine(ink.copy(alpha = .18f), Offset(left + width * .12f, top + height * .035f), Offset(left + width * .12f, top + height * .88f), width * .018f)
        if (decorated) {
            val motif = Offset(center.x + width * .035f, top + height * .43f)
            val r = width * .23f
            when (cover) {
                CollectionCover.Space -> {
                    drawCircle(ink, r, motif, style = Stroke(width * .018f))
                    rotate(-28f, motif) {
                        drawOval(ink, Offset(motif.x - r * 1.5f, motif.y - r * .45f), Size(r * 3, r * .9f), style = Stroke(width * .018f))
                    }
                    drawCircle(ink, width * .035f, motif + Offset(r * 1.5f, -r))
                }
                CollectionCover.Notes -> {
                    repeat(4) { line ->
                        val y = motif.y - r + line * r * .58f
                        drawLine(ink, Offset(motif.x - r, y), Offset(motif.x + r * if (line == 3) .3f else 1f, y), width * .025f, StrokeCap.Round)
                    }
                }
                else -> {
                    val nodes = (0..5).map { index ->
                        val rad = Math.PI / 3 * index - Math.PI / 6
                        motif + Offset(cos(rad).toFloat() * r, sin(rad).toFloat() * r)
                    }
                    nodes.forEachIndexed { index, node ->
                        drawLine(ink, node, nodes[(index + 1) % nodes.size], width * .018f)
                        drawCircle(color, width * .045f, node)
                        drawCircle(ink, width * .028f, node)
                    }
                    if (cover == CollectionCover.Reactions) drawShelfSparkle(motif, r * .4f, ink)
                    else drawLine(ink, nodes[1], motif + Offset(r * .9f, r * 1.6f), width * .018f)
                }
            }
            drawLine(ink.copy(alpha = .75f), Offset(left + width * .29f, top + height * .73f), Offset(left + width * .8f, top + height * .73f), width * .025f, StrokeCap.Round)
            drawLine(ink.copy(alpha = .35f), Offset(left + width * .4f, top + height * .79f), Offset(left + width * .69f, top + height * .79f), width * .018f, StrokeCap.Round)
        }
    }
}

private fun DrawScope.drawShelfSparkle(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticBezierTo(center.x + radius * .2f, center.y - radius * .2f, center.x + radius, center.y)
        quadraticBezierTo(center.x + radius * .2f, center.y + radius * .2f, center.x, center.y + radius)
        quadraticBezierTo(center.x - radius * .2f, center.y + radius * .2f, center.x - radius, center.y)
        quadraticBezierTo(center.x - radius * .2f, center.y - radius * .2f, center.x, center.y - radius)
        close()
    }
    drawPath(path, color)
}
