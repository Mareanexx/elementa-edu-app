package ru.mareanexx.collections_impl.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.mareanexx.collections_api.domain.model.CollectionCover
import ru.mareanexx.collections_impl.R
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun CollectionArtwork(cover: CollectionCover, modifier: Modifier = Modifier) {
    val colors = when (cover) {
        CollectionCover.Chemistry -> listOf(Color(0xFF39363C), Color(0xFF514654))
        CollectionCover.Molecules -> listOf(Color(0xFF7BBED0), Color(0xFF365DA1))
        CollectionCover.Reactions -> listOf(Color(0xFFD699AA), Color(0xFF8B60A7))
        CollectionCover.Laboratory -> listOf(Color(0xFF6DADA9), Color(0xFF397B85))
        CollectionCover.Notes -> listOf(Color(0xFFA3A0DA), Color(0xFF7065AD))
        CollectionCover.Space -> listOf(Color(0xFF7587BB), Color(0xFF3F416F))
    }
    BoxWithConstraints(modifier.background(Brush.linearGradient(colors)), contentAlignment = Alignment.Center) {
        if (cover == CollectionCover.Chemistry) Image(painterResource(R.drawable.collection_1_example), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else {
            Canvas(Modifier.fillMaxSize()) {
                val points = listOf(Offset(size.width * .1f, size.height * .25f), Offset(size.width * .3f, size.height * .12f), Offset(size.width * .5f, size.height * .4f), Offset(size.width * .75f, size.height * .2f), Offset(size.width * .9f, size.height * .55f), Offset(size.width * .68f, size.height * .8f), Offset(size.width * .35f, size.height * .75f))
                points.zipWithNext().forEach { (a, b) -> drawLine(Color.White.copy(alpha = .25f), a, b, strokeWidth = 2.dp.toPx()) }
                points.forEach { drawCircle(Color.White.copy(alpha = .4f), 3.dp.toPx(), it) }
            }
            Icon(painterResource(when (cover) {
                CollectionCover.Notes -> CommonR.drawable.stylus_note_icon
                CollectionCover.Space -> CommonR.drawable.public_icon
                CollectionCover.Reactions -> CommonR.drawable.star_shine_icon
                CollectionCover.Laboratory -> CommonR.drawable.menu_book_icon
                else -> CommonR.drawable.local_library_icon
            }), null, Modifier.size(minOf(64.dp, minOf(maxWidth, maxHeight) * .5f)), tint = Color.White.copy(alpha = .9f))
        }
    }
}
