package ru.mareanexx.catalog_impl.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import ru.mareanexx.catalog_api.domain.model.CatalogBook
import ru.mareanexx.catalog_impl.R
import ru.mareanexx.ui.theme.*
import kotlin.math.*

@Composable
internal fun CatalogCover(book: CatalogBook, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(12.dp))) {
        if (book.coverUrl == null) CoverPlaceholder(book)
        else SubcomposeAsyncImage(book.coverUrl, stringResource(R.string.catalog_cover, book.title), Modifier.fillMaxSize(), contentScale = ContentScale.Fit,
            loading = { CoverPlaceholder(book) }, error = { CoverPlaceholder(book) })
    }
}
@Composable
private fun CoverPlaceholder(book: CatalogBook) {
    val colors = when (book.coverKey) {
        "molecules" -> listOf(Color(0xFF6B9B9E), Color(0xFF355B6F))
        "space" -> listOf(Color(0xFF8F80BB), Color(0xFF484477))
        "reactions" -> listOf(Color(0xFFC98D91), Color(0xFF97577C))
        "laboratory" -> listOf(Color(0xFF709E8E), Color(0xFF35685F))
        "notes" -> listOf(Color(0xFFD3A36D), Color(0xFF9A6C59))
        else -> listOf(Color(0xFF8586B2), Color(0xFF414D7A))
    }
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(colors))) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension * .28f
            val center = Offset(size.width * .55f, size.height * .62f)
            val points = (0..5).map { i -> Offset(center.x + cos(i * PI / 3).toFloat() * radius, center.y + sin(i * PI / 3).toFloat() * radius) }
            points.forEachIndexed { index, point ->
                drawLine(Color.White.copy(alpha = .42f), point, points[(index + 1) % 6], 1.5.dp.toPx())
                drawCircle(Color.White.copy(alpha = .75f), radius * .09f, point)
            }
            drawLine(Color.White.copy(alpha = .22f), Offset(size.width * .07f, 0f), Offset(size.width * .07f, size.height), 1.dp.toPx())
            drawCircle(Color.White.copy(alpha = .07f), size.width * .7f, Offset(size.width, size.height))
        }
        Text(book.title, Modifier.padding(10.dp), color = Color.White, fontFamily = BoldNunito, fontSize = 11.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
