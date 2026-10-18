package ru.mareanexx.book_impl.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import ru.mareanexx.book_impl.R
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun BookCover(url: String?, title: String, modifier: Modifier = Modifier) {
    val strings = LocalResources.current
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(descriptionBackground), contentAlignment = Alignment.Center) {
        if (url == null) MissingCover()
        else SubcomposeAsyncImage(url, strings.getString(R.string.book_cover, title), Modifier.fillMaxSize(), contentScale = ContentScale.Fit,
            loading = {}, error = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { MissingCover() } })
    }
}

@Composable
private fun MissingCover() {
    val strings = LocalResources.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(painterResource(CommonR.drawable.menu_book_icon), null, Modifier.size(38.dp), tint = deselectedTab)
        Text(strings.getString(R.string.book_cover_missing), Modifier.padding(top = 8.dp), color = titleMain, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
    }
}
