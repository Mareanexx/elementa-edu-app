package ru.mareanexx.catalog_impl.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.catalog_api.domain.model.CatalogCategory
import ru.mareanexx.ui.theme.*
import kotlin.math.*

@Composable
internal fun CategoryTile(category: CatalogCategory, onClick: () -> Unit) {
    Column(Modifier.width(88.dp).catalogClick(onClick = onClick).padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        CategoryArt(category.iconKey, Modifier.size(76.dp))
        Text(category.title, color = titleMain, fontFamily = BoldNunito, fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = 0.sp, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
internal fun CategoryArt(key: String, modifier: Modifier = Modifier) {
    val palette = when (key) {
        "organic" -> Color(0xFFD7EEE5) to Color(0xFF4F9985)
        "inorganic" -> Color(0xFFFFE3D4) to Color(0xFFCF896B)
        "physical" -> Color(0xFFE0DCF7) to Color(0xFF8880CA)
        "analytical" -> Color(0xFFD8ECF7) to Color(0xFF5C99B8)
        "biochemistry" -> Color(0xFFF9DFE7) to Color(0xFFC681A5)
        else -> Color(0xFFF5EACF) to Color(0xFFBFA161)
    }
    Canvas(modifier.background(palette.first, CircleShape).padding(15.dp)) {
        val ink = palette.second
        val stroke = 2.5.dp.toPx()
        val w = size.width; val h = size.height
        fun bond(a: Offset, b: Offset) { drawLine(ink, a, b, stroke, StrokeCap.Round) }
        when (key) {
            "physical" -> {
                repeat(3) { rotate(it * 60f) { drawOval(ink, Offset(0f, h * .3f), Size(w, h * .4f), style = Stroke(stroke)) } }
                drawCircle(ink, w * .105f)
                drawCircle(Color.White, w * .075f, Offset(w * .85f, h * .27f))
            }
            "analytical" -> {
                val path = Path().apply { moveTo(w * .4f, h * .08f); lineTo(w * .4f, h * .4f); lineTo(w * .14f, h * .85f); quadraticTo(w * .1f, h * .95f, w * .25f, h * .95f); lineTo(w * .8f, h * .95f); quadraticTo(w * .94f, h * .95f, w * .86f, h * .81f); lineTo(w * .6f, h * .4f); lineTo(w * .6f, h * .08f) }
                drawPath(path, ink, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
                bond(Offset(w * .31f, h * .66f), Offset(w * .7f, h * .66f))
                bond(Offset(w * .32f, h * .08f), Offset(w * .68f, h * .08f))
                drawCircle(ink.copy(alpha = .5f), w * .055f, Offset(w * .48f, h * .8f))
            }
            "inorganic" -> {
                val path = Path().apply { moveTo(w * .25f, h * .12f); lineTo(w * .78f, h * .12f); lineTo(w * .98f, h * .42f); lineTo(w * .5f, h * .95f); lineTo(w * .02f, h * .42f); close() }
                drawPath(path, ink.copy(alpha = .2f)); drawPath(path, ink, style = Stroke(stroke, join = StrokeJoin.Round))
                bond(Offset(w * .02f, h * .42f), Offset(w * .98f, h * .42f)); bond(Offset(w * .3f, h * .42f), Offset(w * .5f, h * .95f)); bond(Offset(w * .7f, h * .42f), Offset(w * .5f, h * .95f))
            }
            "biochemistry" -> repeat(5) { i ->
                val y = h * (.1f + i * .2f)
                val x = w * (.5f + sin(i * PI / 2).toFloat() * .3f)
                bond(Offset(x, y), Offset(w - x, y)); drawCircle(ink, w * .07f, Offset(x, y)); drawCircle(ink.copy(alpha = .6f), w * .07f, Offset(w - x, y))
                if (i < 4) { val next = w * (.5f + sin((i + 1) * PI / 2).toFloat() * .3f); bond(Offset(x, y), Offset(next, y + h * .2f)); bond(Offset(w - x, y), Offset(w - next, y + h * .2f)) }
            }
            else -> {
                val points = (0..5).map { Offset(w * .5f + cos(it * PI / 3).toFloat() * w * .36f, h * .5f + sin(it * PI / 3).toFloat() * h * .36f) }
                points.forEachIndexed { i, point -> bond(point, points[(i + 1) % 6]); drawCircle(ink, w * .065f, point) }
                drawCircle(Color.White.copy(alpha = .8f), w * .08f, Offset(w * .5f, h * .5f))
            }
        }
    }
}
