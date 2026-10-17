package ru.mareanexx.collections_impl.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.mareanexx.collections_api.domain.model.CollectionCover
import ru.mareanexx.collections_impl.R
import ru.mareanexx.collections_impl.presentation.CollectionsIntent
import ru.mareanexx.collections_impl.presentation.model.CollectionEditorState
import ru.mareanexx.ui.theme.*

@Composable
internal fun CollectionEditorDialog(editor: CollectionEditorState, busy: Boolean, onIntent: (CollectionsIntent) -> Unit, onChoosePhoto: () -> Unit) {
    val strings = LocalResources.current
    Dialog({ if (!busy) onIntent(CollectionsIntent.DismissEditor) }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.padding(16.dp).widthIn(max = 440.dp).imePadding(), shape = RoundedCornerShape(28.dp), color = backgroundSecondary) {
            Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(strings.getString(if (editor.id == null) R.string.collections_create_title else R.string.collections_edit_title), color = titleMain, style = MaterialTheme.typography.titleMedium)
                Text(strings.getString(R.string.collections_create_hint), color = username, style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(editor.form.name, { onIntent(CollectionsIntent.ChangeName(it)) }, enabled = !busy, singleLine = true, textStyle = MaterialTheme.typography.labelLarge.copy(color = titleMain),
                    label = { Text(strings.getString(R.string.collections_name)) }, modifier = Modifier.fillMaxWidth().testTag("collection_name"), shape = RoundedCornerShape(16.dp),
                    isError = editor.fieldError != null,
                    supportingText = { Text(editor.fieldError?.let { collectionsError(it) } ?: strings.getString(R.string.collections_name_caption, 60), color = if (editor.fieldError != null) Color(0xFFB4485D) else username) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { onIntent(CollectionsIntent.Save) }))
                Text(strings.getString(R.string.collections_cover), style = MaterialTheme.typography.displaySmall, color = titleMain)
                if (editor.form.image != null) {
                    CollectionCoverImage(editor.form.cover, editor.form.image, Modifier.fillMaxWidth().height(104.dp).clip(RoundedCornerShape(16.dp)).testTag("collection_cover_preview"))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onChoosePhoto, enabled = !busy) { Text(strings.getString(R.string.collections_photo_change), color = bodySecondary) }
                        TextButton({ onIntent(CollectionsIntent.ChangeCover(editor.form.cover)) }, enabled = !busy) { Text(strings.getString(R.string.collections_photo_remove), color = username) }
                    }
                } else OutlinedButton(onChoosePhoto, enabled = !busy, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    if (editor.coverBusy) CircularProgressIndicator(Modifier.size(18.dp), color = bodySecondary, strokeWidth = 2.dp)
                    else Text(strings.getString(R.string.collections_photo_choose), color = bodySecondary)
                }
                CollectionCover.entries.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { cover ->
                            val label = coverTitle(cover)
                            Column(Modifier.weight(1f).selectable(cover == editor.form.cover && editor.form.image == null, enabled = !busy, role = Role.RadioButton,
                                onClick = { onIntent(CollectionsIntent.ChangeCover(cover)) }).semantics { contentDescription = label }) {
                                CollectionShelfArtwork(cover, Modifier.fillMaxWidth().height(62.dp).clip(RoundedCornerShape(12.dp))
                                    .border(if (cover == editor.form.cover && editor.form.image == null) 3.dp else 0.dp, if (cover == editor.form.cover && editor.form.image == null) bodySecondary else Color.Transparent, RoundedCornerShape(12.dp)))
                                Text(label, Modifier.padding(top = 4.dp), color = if (cover == editor.form.cover && editor.form.image == null) bodySecondary else username, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                }
                editor.requestError?.let { CollectionsErrorCard(it) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton({ onIntent(CollectionsIntent.DismissEditor) }, enabled = !busy) { Text(strings.getString(R.string.collections_cancel), color = bodySecondary) }
                    CollectionsPrimaryButton(strings.getString(if (editor.id == null) R.string.collections_create else R.string.collections_save), loading = busy,
                        modifier = Modifier.weight(1f), onClick = { onIntent(CollectionsIntent.Save) })
                }
            }
        }
    }
}

@Composable
private fun coverTitle(cover: CollectionCover): String = LocalResources.current.getString(when (cover) {
    CollectionCover.Chemistry -> R.string.collections_cover_chemistry
    CollectionCover.Molecules -> R.string.collections_cover_molecules
    CollectionCover.Reactions -> R.string.collections_cover_reactions
    CollectionCover.Laboratory -> R.string.collections_cover_laboratory
    CollectionCover.Notes -> R.string.collections_cover_notes
    CollectionCover.Space -> R.string.collections_cover_space
})
