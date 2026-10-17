package ru.mareanexx.collections_impl.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import ru.mareanexx.collections_api.domain.model.CollectionCover
import ru.mareanexx.collections_api.domain.model.CollectionImage

@Composable
internal fun CollectionCoverImage(cover: CollectionCover, image: CollectionImage?, modifier: Modifier = Modifier) {
    Box(modifier) {
        CollectionShelfArtwork(cover, Modifier.fillMaxSize())
        if (image != null) AsyncImage(image.url, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}
