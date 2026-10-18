package ru.mareanexx.reader_impl.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import ru.mareanexx.reader_impl.R
import ru.mareanexx.reader_impl.presentation.model.*
import ru.mareanexx.reader_impl.presentation.engine.ReaderController
import ru.mareanexx.ui.theme.SecondaryScreenTitle
import ru.mareanexx.common_ui.R as CommonR

@Composable
fun ReaderInventory(state: ReaderState, controller: ReaderController, accept: (ReaderIntent) -> Unit, synchronize: () -> Unit) {
    val palette = readerPalette(state.settings.theme)
    val notes = state.panel == ReaderPanel.Notes
    Column(Modifier.fillMaxSize().background(palette.paper).safeDrawingPadding().testTag("reader_inventory")) {
        Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton({ accept(ReaderIntent.Panel(ReaderPanel.None)) }) { Icon(painterResource(CommonR.drawable.arrow_back_icon), stringResource(R.string.reader_back), tint = palette.ink) }
            Text(stringResource(if (notes) R.string.reader_notes else R.string.reader_contents), style = SecondaryScreenTitle, color = palette.ink)
        }
        if (notes) {
            TextButton(synchronize, Modifier.padding(horizontal = 8.dp)) { Text(stringResource(when { state.sync.syncing -> R.string.reader_syncing; state.sync.error != null -> R.string.reader_sync_retry; state.sync.pending > 0 -> R.string.reader_sync_pending; else -> R.string.reader_saved_local }), color = palette.accent) }
            if (state.annotations.isEmpty()) Column(Modifier.fillMaxWidth().weight(1f).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(painterResource(CommonR.drawable.stylus_note_icon), null, Modifier.size(64.dp), tint = palette.accent)
                Text(stringResource(R.string.reader_notes_empty), Modifier.padding(top = 20.dp), style = MaterialTheme.typography.titleSmall, color = palette.ink)
                Text(stringResource(R.string.reader_notes_empty_hint), Modifier.padding(top = 12.dp), color = palette.muted, style = MaterialTheme.typography.labelLarge)
            } else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.annotations, key = { it.id }) { annotation ->
                    Surface(color = palette.surface, shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(annotation.anchor.quote, Modifier.fillMaxWidth().background(highlightColor(annotation.color).copy(alpha = .28f), RoundedCornerShape(8.dp)).padding(10.dp), color = palette.ink, maxLines = 4, overflow = TextOverflow.Ellipsis, fontSize = 15.sp, lineHeight = 22.sp)
                            annotation.note?.let { Text(it, Modifier.padding(top = 12.dp), color = palette.ink, maxLines = 6, overflow = TextOverflow.Ellipsis, fontSize = 16.sp, lineHeight = 23.sp) }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                if (annotation.anchor.version == state.document?.version) TextButton({ controller.jump(annotation.anchor.start); accept(ReaderIntent.Panel(ReaderPanel.None)) }) { Text(stringResource(R.string.reader_open_quote), color = palette.accent) }
                                else Text(stringResource(R.string.reader_other_version), Modifier.weight(1f).padding(vertical = 12.dp), color = palette.muted, style = MaterialTheme.typography.labelSmall)
                                IconButton({ accept(ReaderIntent.Annotation(annotation.id)) }) { Icon(painterResource(CommonR.drawable.more_horiz_icon), stringResource(R.string.reader_annotation_actions), tint = palette.ink) }
                            }
                        }
                    }
                }
            }
        } else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val chapters = state.document?.chapters.orEmpty()
            itemsIndexed(chapters, key = { _, value -> value.id }) { index, chapter ->
                val current = state.position?.chapterId == chapter.id
                Surface(
                    onClick = { controller.chapter(chapter.id); accept(ReaderIntent.Panel(ReaderPanel.None)) },
                    modifier = Modifier.semantics { selected = current },
                    color = if (current) palette.ink.copy(alpha = .06f) else palette.surface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, palette.ink.copy(alpha = if (current) .14f else .04f)),
                ) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).background(palette.ink.copy(alpha = .06f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Text((index + 1).toString().padStart(2, '0'), style = MaterialTheme.typography.labelMedium, color = palette.muted)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (current) Text(stringResource(R.string.reader_current_chapter), color = palette.muted, style = MaterialTheme.typography.labelSmall)
                            Text(chapter.title.ifBlank { stringResource(R.string.reader_chapter_title, index + 1) }, color = palette.ink, style = MaterialTheme.typography.displaySmall)
                        }
                        Icon(painterResource(if (current) R.drawable.reader_check else CommonR.drawable.keyboard_arrow_right_icon), null, Modifier.size(20.dp), tint = palette.muted)
                    }
                }
            }
        }
    }
}
