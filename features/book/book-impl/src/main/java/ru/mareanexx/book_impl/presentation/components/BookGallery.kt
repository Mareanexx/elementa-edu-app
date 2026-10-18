package ru.mareanexx.book_impl.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import ru.mareanexx.book_api.domain.model.BookImage
import ru.mareanexx.book_impl.R
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun BookGallery(images: List<BookImage>, initial: Int, onPage: (Int) -> Unit, onClose: () -> Unit) {
    if (images.isEmpty()) return
    val strings = LocalResources.current
    val preferences = LocalAppUiPreferences.current
    val pager = rememberPagerState(initialPage = initial.coerceIn(images.indices)) { images.size }
    val scope = rememberCoroutineScope()
    var zoomed by remember { mutableStateOf(false) }
    LaunchedEffect(pager.currentPage) { onPage(pager.currentPage) }
    fun move(page: Int) { scope.launch { if (preferences.reduceMotion) pager.scrollToPage(page) else pager.animateScrollToPage(page) } }
    Dialog(onClose, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        val view = LocalView.current
        DisposableEffect(view) {
            val window = (view.parent as? DialogWindowProvider)?.window
            val controller = window?.let { WindowCompat.getInsetsController(it, view) }
            val oldStatus = controller?.isAppearanceLightStatusBars
            val oldNav = controller?.isAppearanceLightNavigationBars
            controller?.isAppearanceLightStatusBars = false
            controller?.isAppearanceLightNavigationBars = false
            onDispose { if (oldStatus != null) controller.isAppearanceLightStatusBars = oldStatus; if (oldNav != null) controller.isAppearanceLightNavigationBars = oldNav }
        }
        Column(Modifier.fillMaxSize().background(Color(0xFF211D37)).safeDrawingPadding().testTag("book_gallery")) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClose) { Icon(painterResource(CommonR.drawable.close_icon), strings.getString(R.string.book_close), tint = Color.White) }
                Text(strings.getString(R.string.book_gallery_count, pager.currentPage + 1, images.size), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.width(48.dp))
            }
            HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth().clipToBounds().testTag("book_gallery_pager"), userScrollEnabled = !zoomed, key = { images[it].id }) { page ->
                ZoomableBookImage(images[page], page == pager.currentPage) { zoomed = it }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton({ move(pager.currentPage - 1) }, enabled = pager.currentPage > 0) { Icon(painterResource(CommonR.drawable.arrow_back_icon), strings.getString(R.string.book_gallery_prev), tint = if (pager.currentPage > 0) Color.White else Color.White.copy(alpha = .25f)) }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(images[pager.currentPage].caption.ifBlank { strings.getString(R.string.book_gallery_page, pager.currentPage + 1, images.size) }, color = Color.White, style = MaterialTheme.typography.labelMedium)
                    if (images.size == 1) Text(strings.getString(R.string.book_gallery_single), color = Color.White.copy(alpha = .6f), style = MaterialTheme.typography.labelSmall)
                }
                IconButton({ move(pager.currentPage + 1) }, enabled = pager.currentPage < images.lastIndex) { Icon(painterResource(CommonR.drawable.arrow_right_alt_icon), strings.getString(R.string.book_gallery_next), tint = if (pager.currentPage < images.lastIndex) Color.White else Color.White.copy(alpha = .25f)) }
            }
            if (images.size > 1) LazyRow(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(images, key = { _, it -> it.id }) { index, image ->
                    AsyncImage(image.url, strings.getString(R.string.book_gallery_thumb, index + 1), Modifier.size(52.dp, 64.dp).clip(RoundedCornerShape(8.dp))
                        .border(if (index == pager.currentPage) 2.dp else 1.dp, Color.White.copy(alpha = if (index == pager.currentPage) 1f else .15f), RoundedCornerShape(8.dp)).clickable { move(index) }, contentScale = ContentScale.Fit)
                }
            }
            Text(strings.getString(R.string.book_gallery_zoom_hint), Modifier.fillMaxWidth().padding(16.dp), color = Color.White.copy(alpha = .6f), style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun ZoomableBookImage(image: BookImage, active: Boolean, onZoom: (Boolean) -> Unit) {
    val strings = LocalResources.current
    var scale by remember(image.id) { mutableFloatStateOf(1f) }
    var pan by remember(image.id) { mutableStateOf(Offset.Zero) }
    var viewport by remember { mutableStateOf(IntSize(1, 1)) }
    var original by remember { mutableStateOf(IntSize(1, 1)) }
    var retry by remember { mutableIntStateOf(0) }
    LaunchedEffect(scale, active) { if (active) onZoom(scale > 1.01f) }
    LaunchedEffect(viewport) { pan = Offset.Zero }
    val transform = rememberTransformableState { centroid, zoom, delta, _ ->
        val previous = scale
        scale = (scale * zoom).coerceIn(1f, 5f)
        val focus = if (centroid.x.isFinite() && centroid.y.isFinite()) centroid - Offset(viewport.width / 2f, viewport.height / 2f) else Offset.Zero
        val shifted = pan + delta + (focus - pan) * (1f - scale / previous)
        val fit = minOf(viewport.width.toFloat() / original.width, viewport.height.toFloat() / original.height)
        val maxX = ((original.width * fit * scale - viewport.width) / 2).coerceAtLeast(0f)
        val maxY = ((original.height * fit * scale - viewport.height) / 2).coerceAtLeast(0f)
        pan = if (scale == 1f) Offset.Zero else Offset(shifted.x.coerceIn(-maxX, maxX), shifted.y.coerceIn(-maxY, maxY))
    }
    Box(Modifier.fillMaxSize().clipToBounds().testTag("book_gallery_image_${image.id}").semantics { stateDescription = strings.getString(R.string.book_gallery_scale, (scale * 100).toInt()) }.onSizeChanged { viewport = it }
        .transformable(transform, canPan = { scale > 1.01f }).pointerInput(image.id) { detectTapGestures(onDoubleTap = { scale = if (scale > 1f) 1f else 2.5f; pan = Offset.Zero }) }, contentAlignment = Alignment.Center) {
        key(image.id, retry) {
            SubcomposeAsyncImage(image.url, image.caption, Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale; translationX = pan.x; translationY = pan.y }, contentScale = ContentScale.Fit,
                onSuccess = { original = IntSize(it.result.image.width.coerceAtLeast(1), it.result.image.height.coerceAtLeast(1)) },
                loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(28.dp), color = Color.White) } },
                error = { Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(strings.getString(R.string.book_gallery_load_error), color = Color.White, style = MaterialTheme.typography.labelMedium)
                    TextButton({ retry++ }) { Text(strings.getString(R.string.book_retry), color = Color.White) }
                } })
        }
    }
}
