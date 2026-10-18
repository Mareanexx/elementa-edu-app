package ru.mareanexx.reader_impl.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import ru.mareanexx.reader_impl.R
import ru.mareanexx.reader_api.domain.validation.ReaderRules
import ru.mareanexx.reader_impl.presentation.model.*
import ru.mareanexx.ui.theme.SecondaryScreenTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderNoteSheet(state: ReaderState, accept: (ReaderIntent) -> Unit) {
    val draft = state.note ?: return
    val palette = readerPalette(state.settings.theme)
    ModalBottomSheet(onDismissRequest = { accept(ReaderIntent.DismissNote) }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.surface, contentColor = palette.ink, dragHandle = { Box(Modifier.padding(top = 8.dp, bottom = 4.dp).size(28.dp, 3.dp).background(palette.muted.copy(alpha = .3f), CircleShape)) },
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false), contentWindowInsets = { WindowInsets.safeDrawing }) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.91f).imePadding().padding(horizontal = 16.dp).testTag("reader_note_editor")) {
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.reader_note_title), Modifier.weight(1f), style = SecondaryScreenTitle)
                FilledIconButton({ accept(ReaderIntent.SaveNote) }, Modifier.size(48.dp).testTag("reader_note_save"), enabled = draft.text.isNotBlank() && !draft.saving,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = palette.accent, contentColor = palette.surface)) {
                    if (draft.saving) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = palette.surface)
                    else Icon(painterResource(R.drawable.reader_check), stringResource(R.string.reader_note_save), Modifier.size(26.dp))
                }
            }
            Row(Modifier.fillMaxWidth().heightIn(max = 200.dp).verticalScroll(rememberScrollState()).padding(vertical = 12.dp)) {
                Box(Modifier.width(4.dp).heightIn(min = 56.dp).background(highlightColor(draft.color), RoundedCornerShape(2.dp)))
                Text(draft.anchor.quote, Modifier.padding(start = 14.dp), fontSize = 17.sp, lineHeight = 25.sp, color = palette.ink)
            }
            state.operationError?.let { Text(readerErrorText(it), Modifier.padding(vertical = 8.dp), color = androidx.compose.ui.graphics.Color(0xFFBD6B83), style = MaterialTheme.typography.labelMedium) }
            TextField(draft.text, { accept(ReaderIntent.NoteText(it)) }, Modifier.fillMaxWidth().weight(1f).testTag("reader_note_input"),
                placeholder = { Text(stringResource(R.string.reader_note_hint), color = palette.muted) },
                colors = TextFieldDefaults.colors(focusedContainerColor = palette.surface, unfocusedContainerColor = palette.surface, focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent, unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent, focusedTextColor = palette.ink, unfocusedTextColor = palette.ink, cursorColor = palette.accent),
                textStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp, lineHeight = 25.sp))
            Text(stringResource(R.string.reader_note_counter, draft.text.length, ReaderRules.MAX_NOTE), Modifier.align(Alignment.End).padding(bottom = 16.dp), color = palette.muted, style = MaterialTheme.typography.labelSmall)
        }
    }
    if (state.confirmDiscard) AlertDialog(onDismissRequest = { accept(ReaderIntent.KeepNote) }, title = { Text(stringResource(R.string.reader_discard_title)) }, text = { Text(stringResource(R.string.reader_discard_text)) },
        confirmButton = { TextButton({ accept(ReaderIntent.KeepNote) }) { Text(stringResource(R.string.reader_continue_editing)) } }, dismissButton = { TextButton({ accept(ReaderIntent.DiscardNote) }) { Text(stringResource(R.string.reader_discard)) } })
}
