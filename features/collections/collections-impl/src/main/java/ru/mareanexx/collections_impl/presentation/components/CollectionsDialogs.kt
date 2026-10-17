package ru.mareanexx.collections_impl.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import ru.mareanexx.collections_impl.R
import ru.mareanexx.collections_impl.presentation.CollectionsIntent
import ru.mareanexx.collections_impl.presentation.model.CollectionsUiState
import ru.mareanexx.ui.theme.*

@Composable
internal fun CollectionsDialogs(state: CollectionsUiState, onIntent: (CollectionsIntent) -> Unit, onChoosePhoto: () -> Unit) {
    val strings = LocalResources.current
    state.editor?.let { CollectionEditorDialog(it, state.busy || it.coverBusy, onIntent, onChoosePhoto) }
    if (state.settingsOpen || state.sortOpen) CollectionsSettingsSheet(state, onIntent)
    if (state.picker != null) CollectionBookPicker(state, onIntent)
    val deleting = state.items.firstOrNull { it.id == state.deleteId }
    val removing = state.books.firstOrNull { it.id == state.removeBookId }
    if (deleting != null || removing != null) {
        val dismiss = if (deleting != null) CollectionsIntent.DismissDelete else CollectionsIntent.DismissRemoveBook
        AlertDialog(onDismissRequest = { if (!state.busy) onIntent(dismiss) }, containerColor = backgroundSecondary,
            title = { Text(strings.getString(if (deleting != null) R.string.collections_delete_title else R.string.collections_remove_book_title), color = titleMain) },
            text = { Column {
                Text(strings.getString(if (deleting != null) R.string.collections_delete_body else R.string.collections_remove_book_body, deleting?.let { collectionTitle(it) } ?: removing!!.title), color = username)
                state.operationError?.let { Spacer(Modifier.height(12.dp)); CollectionsErrorCard(it) }
            } },
            dismissButton = { TextButton({ onIntent(dismiss) }, enabled = !state.busy) { Text(strings.getString(R.string.collections_cancel), color = bodySecondary) } },
            confirmButton = { TextButton({ onIntent(if (deleting != null) CollectionsIntent.ConfirmDelete else CollectionsIntent.ConfirmRemoveBook) }, enabled = !state.busy) {
                if (state.busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = bodySecondary)
                else Text(strings.getString(if (deleting != null) R.string.collections_delete else R.string.collections_remove_book), color = Color(0xFFB4485D))
            } })
    }
}
