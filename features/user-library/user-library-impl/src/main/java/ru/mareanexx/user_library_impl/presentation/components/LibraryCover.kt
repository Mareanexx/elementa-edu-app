package ru.mareanexx.user_library_impl.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_impl.R
import ru.mareanexx.user_library_impl.presentation.LibraryIntent
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import coil3.compose.SubcomposeAsyncImage
import kotlin.math.*

@Composable
internal fun LibraryCover(book: LibraryBook, modifier: Modifier = Modifier) {
    val strings = LocalResources.current
    Box(modifier.clip(RoundedCornerShape(12.dp))) {
        if (book.coverUrl == null) CoverPlaceholder(book)
        else SubcomposeAsyncImage(book.coverUrl, strings.getString(R.string.library_cover, book.title), Modifier.fillMaxSize(), contentScale = ContentScale.Fit,
            loading = { CoverPlaceholder(book) }, error = { CoverPlaceholder(book) })
    }
}

@Composable
private fun CoverPlaceholder(book: LibraryBook) {
    val colors = when (book.coverKey) {
        "molecules" -> listOf(Color(0xFF6B9B9E), Color(0xFF355B6F))
        "space" -> listOf(Color(0xFF8F80BB), Color(0xFF484477))
        "reactions" -> listOf(Color(0xFFC98D91), Color(0xFF97577C))
        "laboratory" -> listOf(Color(0xFF709E8E), Color(0xFF35685F))
        "notes" -> listOf(Color(0xFFD3A36D), Color(0xFF9A6C59))
        else -> listOf(Color(0xFF8586B2), Color(0xFF414D7A))
    }
    val missing = LocalResources.current.getString(R.string.library_cover_placeholder)
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(colors)).semantics { contentDescription = missing }) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension * .28f
            val center = Offset(size.width * .55f, size.height * .60f)
            val points = (0..5).map { i -> Offset(center.x + cos(i * PI / 3).toFloat() * radius, center.y + sin(i * PI / 3).toFloat() * radius) }
            points.forEachIndexed { index, point ->
                drawLine(Color.White.copy(alpha = .42f), point, points[(index + 1) % 6], 1.5.dp.toPx())
                drawCircle(Color.White.copy(alpha = .75f), radius * .09f, point)
            }
            drawLine(Color.White.copy(alpha = .22f), Offset(size.width * .07f, 0f), Offset(size.width * .07f, size.height), 1.dp.toPx())
            drawCircle(Color.White.copy(alpha = .07f), size.width * .7f, Offset(size.width, size.height))
        }
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(book.title, color = Color.White.copy(alpha = .95f), style = MaterialTheme.typography.displaySmall, fontSize = 12.sp, lineHeight = 15.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Box(Modifier.padding(top = 8.dp).width(24.dp).height(2.dp).background(Color.White.copy(alpha = .5f)))
        }
    }
}
