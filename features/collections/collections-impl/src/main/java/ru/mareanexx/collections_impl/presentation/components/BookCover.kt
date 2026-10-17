package ru.mareanexx.collections_impl.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ru.mareanexx.collections_api.domain.model.CollectionBook

@Composable
internal fun BookCover(book: CollectionBook, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(12.dp))) {
        CollectionArtwork(book.cover, Modifier.fillMaxSize())
        if (book.coverUrl != null) AsyncImage(book.coverUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}
