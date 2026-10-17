package ru.mareanexx.user_profile_impl.presentation.photo

import ru.mareanexx.user_profile_impl.R
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_api.domain.model.*
import ru.mareanexx.user_profile_impl.presentation.components.*
import kotlin.math.roundToInt

@Composable
internal fun AvatarEditorScreen(uri: String, busy: Boolean, failure: ProfileFailure?, onUpload: (AvatarUpload) -> Unit, onBack: () -> Unit) {
    val strings = androidx.compose.ui.platform.LocalResources.current
    val context = LocalContext.current
    val processor = remember(context) { AvatarImageProcessor(context) }
    val scope = rememberCoroutineScope()
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var rotation by rememberSaveable(uri) { mutableIntStateOf(0) }
    var zoom by rememberSaveable(uri) { mutableFloatStateOf(1f) }
    var offsetX by rememberSaveable(uri) { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable(uri) { mutableFloatStateOf(0f) }
    var processing by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    val blocked = busy || processing
    LaunchedEffect(uri, rotation) {
        bitmap = null
        loadError = null
        try {
            bitmap = withContext(Dispatchers.IO) { processor.load(uri, rotation) }
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            loadError = strings.getString(R.string.profile_photo_load_error)
        }
    }
    BackHandler(enabled = blocked) {}
    Scaffold(containerColor = backgroundSecondary, topBar = { ProfileTopBar(strings.getString(R.string.profile_photo_title), { if (!blocked) onBack() }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(strings.getString(R.string.profile_photo_heading), color = titleMain, style = MaterialTheme.typography.displayMedium)
            Text(strings.getString(R.string.profile_photo_hint), color = username, style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
            val image = bitmap
            Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(24.dp)).background(authFieldBackground), contentAlignment = Alignment.Center) {
                if (image == null) {
                    if (loadError == null) CircularProgressIndicator(color = bodySecondary)
                    else Text(requireNotNull(loadError), Modifier.padding(24.dp), color = username)
                } else {
                    val crop = AvatarCrop(zoom, offsetX, offsetY)
                    Canvas(Modifier.fillMaxSize().semantics { contentDescription = strings.getString(R.string.profile_photo_preview) }
                        .pointerInput(image, blocked) {
                            if (!blocked) detectTransformGestures { _, pan, change, _ ->
                                val next = AvatarCrop((zoom * change).coerceIn(1f, 3f), offsetX + pan.x / size.width, offsetY + pan.y / size.height).clamp(image.width, image.height)
                                zoom = next.zoom; offsetX = next.offsetX; offsetY = next.offsetY
                            }
                        }) {
                        val scale = maxOf(size.width / image.width, size.height / image.height) * crop.zoom
                        val width = image.width * scale
                        val height = image.height * scale
                        drawImage(image.asImageBitmap(), dstOffset = IntOffset(((size.width - width) / 2 + crop.offsetX * size.width).roundToInt(),
                            ((size.height - height) / 2 + crop.offsetY * size.height).roundToInt()), dstSize = IntSize(width.roundToInt(), height.roundToInt()), filterQuality = FilterQuality.High)
                        val mask = Path().apply {
                            fillType = PathFillType.EvenOdd
                            addRect(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
                            addOval(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
                        }
                        drawPath(mask, Color.Black.copy(alpha = 0.5f))
                        drawCircle(Color.White.copy(alpha = 0.8f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { rotation = (rotation + 90) % 360; zoom = 1f; offsetX = 0f; offsetY = 0f }, enabled = image != null && !blocked) {
                    Text(strings.getString(R.string.profile_photo_rotate), color = bodySecondary)
                }
                TextButton(onClick = { rotation = 0; zoom = 1f; offsetX = 0f; offsetY = 0f }, enabled = !blocked) { Text(strings.getString(R.string.profile_reset), color = username) }
            }
            Text(strings.getString(R.string.profile_photo_zoom, (zoom * 100).roundToInt()), color = username, style = MaterialTheme.typography.labelMedium)
            Slider(modifier = Modifier.testTag("avatar_zoom"), value = zoom, onValueChange = { value ->
                image?.let {
                    val next = AvatarCrop(value, offsetX, offsetY).clamp(it.width, it.height)
                    zoom = next.zoom; offsetX = next.offsetX; offsetY = next.offsetY
                }
            }, valueRange = 1f..3f, enabled = image != null && !blocked,
                colors = SliderDefaults.colors(thumbColor = bodySecondary, activeTrackColor = bodySecondary))
            if (loadError != null && image != null) Text(requireNotNull(loadError), color = Color(0xFFB4485D), modifier = Modifier.padding(vertical = 8.dp))
            failure?.let { ProfileErrorBanner(it.error); Spacer(Modifier.height(12.dp)) }
            ProfilePrimaryButton(strings.getString(R.string.profile_photo_save), blocked, enabled = image != null) {
                if (image != null) scope.launch {
                    processing = true
                    try {
                        val upload = withContext(Dispatchers.Default) { processor.crop(image, AvatarCrop(zoom, offsetX, offsetY)) }
                        onUpload(upload)
                    } catch (cancelled: kotlinx.coroutines.CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        loadError = strings.getString(R.string.profile_photo_process_error)
                    } finally { processing = false }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
