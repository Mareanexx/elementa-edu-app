package ru.mareanexx.collections_impl.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import ru.mareanexx.common_ui.R as CommonR
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.mareanexx.collections_api.domain.model.CollectionSort
import ru.mareanexx.collections_impl.R
import ru.mareanexx.collections_impl.presentation.CollectionsIntent
import ru.mareanexx.collections_impl.presentation.model.CollectionsUiState
import ru.mareanexx.collections_impl.presentation.screen.components.skeleton.CollectionsSkeleton
import ru.mareanexx.ui.theme.*
import ru.mareanexx.ui.components.feedback.SyncIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CollectionsSettingsSheet(state: CollectionsUiState, onIntent: (CollectionsIntent) -> Unit) {
    val strings = LocalResources.current
    val settings = state.settingsOpen
    ModalBottomSheet(onDismissRequest = { onIntent(if (settings) CollectionsIntent.CloseSettings else CollectionsIntent.CloseSort) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = backgroundSecondary,
        dragHandle = { BottomSheetDefaults.DragHandle(Modifier.height(24.dp)) }) {
        Box(Modifier.fillMaxWidth()) {
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text(strings.getString(if (settings) R.string.collections_settings else R.string.collections_sort), style = MaterialTheme.typography.titleMedium, color = titleMain) }
            item { Text(strings.getString(if (settings) R.string.collections_settings_hint else R.string.collections_sort_hint), style = MaterialTheme.typography.labelMedium, color = username) }
            CollectionSort.entries.forEach { sort -> item {
                Row(Modifier.fillMaxWidth().selectable(state.preferences.sort == sort, enabled = !state.preferencesSaving, role = Role.RadioButton,
                    onClick = { onIntent(CollectionsIntent.SetPreferences(state.preferences.copy(sort = sort))) }).padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(state.preferences.sort == sort, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = bodySecondary))
                    Text(sortTitle(sort), Modifier.padding(start = 12.dp), color = titleMain, style = MaterialTheme.typography.labelLarge)
                }
            } }
            if (settings) item {
                HorizontalDivider(color = authFieldBackground)
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(strings.getString(R.string.collections_show_empty), color = titleMain, style = MaterialTheme.typography.displaySmall)
                        Text(strings.getString(R.string.collections_show_empty_hint), color = username, style = MaterialTheme.typography.labelSmall)
                    }
                    Switch(state.preferences.showEmpty, { onIntent(CollectionsIntent.SetPreferences(state.preferences.copy(showEmpty = it))) }, enabled = !state.preferencesSaving)
                }
            }
            state.operationError?.let { error -> item { CollectionsErrorCard(error) } }
        }
        SyncIndicator(state.preferencesSaving, Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CollectionBookPicker(state: CollectionsUiState, onIntent: (CollectionsIntent) -> Unit) {
    val picker = state.picker ?: return
    val strings = LocalResources.current
    val latestBusy by rememberUpdatedState(state.busy)
    val allowChange = remember { { _: SheetValue -> !latestBusy } }
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = allowChange)
    val books = remember(picker.books, picker.query) { picker.books.filter { picker.query.isBlank() || it.title.contains(picker.query.trim(), true) || it.authors.contains(picker.query.trim(), true) } }
    ModalBottomSheet(onDismissRequest = { if (!state.busy) onIntent(CollectionsIntent.ClosePicker) },
        sheetState = sheet, modifier = Modifier.fillMaxHeight(), sheetGesturesEnabled = !state.busy,
        containerColor = backgroundSecondary, dragHandle = null) {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp).testTag("collection_picker_sheet")) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(strings.getString(R.string.collections_picker_title), Modifier.weight(1f), color = titleMain, style = MaterialTheme.typography.titleMedium)
                IconButton({ onIntent(CollectionsIntent.ClosePicker) }, enabled = !state.busy) {
                    Icon(painterResource(CommonR.drawable.close_icon), strings.getString(R.string.collections_close), tint = titleMain)
                }
            }
            Text(strings.getString(R.string.collections_picker_hint), Modifier.padding(top = 8.dp), color = username, style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(picker.query, { onIntent(CollectionsIntent.SearchBooks(it)) }, enabled = !state.busy, singleLine = true, textStyle = MaterialTheme.typography.labelLarge.copy(color = titleMain), shape = RoundedCornerShape(16.dp),
                placeholder = { Text(strings.getString(R.string.collections_picker_search), style = MaterialTheme.typography.labelMedium) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).testTag("collection_search"))
            if (picker.loading) Box(Modifier.weight(1f)) { CollectionsSkeleton(books = true) }
            else LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                picker.error?.let { error -> item { CollectionsErrorCard(error, if (picker.books.isEmpty()) ({ onIntent(CollectionsIntent.ReloadPicker) }) else null) } }
                if (books.isEmpty() && picker.error == null) item { Text(strings.getString(if (picker.books.isEmpty()) R.string.collections_picker_empty else R.string.collections_picker_not_found), color = username) }
                items(books, key = { it.id }) { book ->
                    val existing = book.id in picker.existing
                    val checked = book.id in picker.selected || existing
                    Surface(shape = RoundedCornerShape(16.dp), color = if (checked) authFieldBackground else androidx.compose.ui.graphics.Color.White) {
                        Row(Modifier.fillMaxWidth().testTag("picker_book_${book.id}").toggleable(checked, enabled = !state.busy && !existing, role = Role.Checkbox,
                            onValueChange = { onIntent(CollectionsIntent.ToggleBook(book.id)) }).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            BookCover(book, Modifier.width(44.dp).height(62.dp))
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(book.title, color = titleMain, style = MaterialTheme.typography.displaySmall, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Text(if (existing) strings.getString(R.string.collections_picker_existing) else book.authors, color = username, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                            Checkbox(checked, onCheckedChange = null, enabled = !existing, colors = CheckboxDefaults.colors(checkedColor = bodySecondary))
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton({ onIntent(CollectionsIntent.ClosePicker) }, enabled = !state.busy) { Text(strings.getString(R.string.collections_cancel), color = bodySecondary) }
                CollectionsPrimaryButton(strings.getString(R.string.collections_picker_add, picker.selected.size), enabled = picker.selected.isNotEmpty() && !picker.loading,
                    loading = state.busy, modifier = Modifier.weight(1f), onClick = { onIntent(CollectionsIntent.AddBooks) })
            }
        }
    }
}
