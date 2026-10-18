package ru.mareanexx.reader_impl.presentation.components

import android.content.ClipData
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import androidx.compose.ui.semantics.*
import kotlinx.coroutines.launch
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_impl.R
import ru.mareanexx.reader_impl.presentation.model.*

@Composable
fun ReaderAnnotationPopup(state: ReaderState, accept: (ReaderIntent) -> Unit) {
    val palette = readerPalette(state.settings.theme)
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val label = stringResource(R.string.reader_quote)
    val density = LocalDensity.current
    Popup(alignment = Alignment.BottomCenter, offset = with(density) { IntOffset(0, -96.dp.roundToPx()) }, onDismissRequest = { accept(ReaderIntent.Panel(ReaderPanel.None)) }, properties = PopupProperties(focusable = true)) {
        Surface(modifier = Modifier.padding(horizontal = 16.dp).widthIn(max = 420.dp).testTag("reader_annotation_menu"), color = palette.surface, contentColor = palette.ink, shape = RoundedCornerShape(24.dp), shadowElevation = 12.dp) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.reader_color), style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HighlightColor.entries.forEach { color ->
                        val colorLabel = stringResource(colorText(color))
                        Surface(modifier = Modifier.size(44.dp).testTag("reader_color_${color.name}").semantics { contentDescription = colorLabel }, onClick = { accept(ReaderIntent.Color(color)) }, shape = CircleShape, color = highlightColor(color), border = BorderStroke(1.dp, palette.ink.copy(alpha = .08f))) {
                            if (state.selectedAnnotation?.color == color) Box(contentAlignment = Alignment.Center) { Icon(painterResource(R.drawable.reader_check), stringResource(colorText(color)), Modifier.size(22.dp), tint = androidx.compose.ui.graphics.Color(0xFF35303F)) }
                            else Spacer(Modifier.fillMaxSize())
                        }
                    }
                }
                state.operationError?.let { Text(readerErrorText(it), color = androidx.compose.ui.graphics.Color(0xFFBD6B83), style = MaterialTheme.typography.labelMedium) }
                if (state.panel == ReaderPanel.Annotation) {
                    TextButton({ accept(ReaderIntent.EditNote) }, Modifier.fillMaxWidth()) { Text(stringResource(if (state.selectedAnnotation?.note == null) R.string.reader_note_add else R.string.reader_edit_note), color = palette.accent) }
                    TextButton({
                        scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(label, state.selectedAnnotation?.anchor?.quote.orEmpty()))) }
                        accept(ReaderIntent.Panel(ReaderPanel.None))
                    }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.reader_copy), color = palette.ink) }
                    TextButton({ accept(ReaderIntent.DeleteAnnotation) }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.reader_delete), color = androidx.compose.ui.graphics.Color(0xFFBB6077)) }
                }
            }
        }
    }
}
