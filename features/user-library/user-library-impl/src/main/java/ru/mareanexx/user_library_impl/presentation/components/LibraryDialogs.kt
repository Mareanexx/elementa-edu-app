package ru.mareanexx.user_library_impl.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.mareanexx.book_api.domain.model.BookEntry
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_impl.R
import ru.mareanexx.user_library_impl.presentation.LibraryIntent
import ru.mareanexx.user_library_impl.presentation.model.LibraryUiState
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryDialogs(state: LibraryUiState, onIntent: (LibraryIntent) -> Unit) {
    val strings = LocalResources.current
    state.actionBook?.let { book ->
        val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet({ onIntent(LibraryIntent.CloseBookMenu) }, sheetState = sheet, containerColor = backgroundSecondary,
            dragHandle = { Box(Modifier.padding(top = 8.dp, bottom = 4.dp).size(32.dp, 4.dp).background(descriptionBackground, RoundedCornerShape(2.dp))) }) {
            Column(Modifier.fillMaxWidth().heightIn(max = LocalConfiguration.current.screenHeightDp.dp * .78f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                Row(Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    LibraryCover(book, Modifier.size(52.dp, 76.dp))
                    Column(Modifier.weight(1f)) {
                        Text(book.title, style = MaterialTheme.typography.titleSmall, color = titleMain, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text(book.authors, Modifier.padding(top = 4.dp), style = MaterialTheme.typography.labelSmall, color = username)
                    }
                }
                MenuAction(strings.getString(R.string.library_open), CommonR.drawable.menu_book_icon) { onIntent(LibraryIntent.OpenBook(book)) }
                MenuAction(strings.getString(when (book.status) {
                    ReadingStatus.NotStarted -> R.string.library_read
                    ReadingStatus.InProgress -> R.string.library_continue
                    ReadingStatus.Completed -> R.string.library_reread
                }), CommonR.drawable.keyboard_arrow_right_icon) { onIntent(LibraryIntent.OpenBook(book, BookEntry.Reader)) }
                MenuAction(strings.getString(R.string.library_to_collection), CommonR.drawable.local_library_icon) { onIntent(LibraryIntent.AddToCollections) }
                MenuAction(strings.getString(if (book.shelves.favorite) R.string.library_favorite_remove else R.string.library_favorite_add), R.drawable.library_heart) { onIntent(LibraryIntent.ToggleFavorite) }
                MenuAction(strings.getString(if (book.shelves.wantToRead) R.string.library_plan_remove else R.string.library_plan_add), CommonR.drawable.bookmark_icon) { onIntent(LibraryIntent.ToggleWantToRead) }
                HorizontalDivider(Modifier.padding(vertical = 6.dp), color = descriptionBackground.copy(alpha = .45f))
                if (book.status != ReadingStatus.Completed) MenuAction(strings.getString(R.string.library_mark_completed), R.drawable.library_check) { onIntent(LibraryIntent.ChangeReadingStatus(ReadingStatus.Completed)) }
                if (book.pagesRead > 0) MenuAction(strings.getString(R.string.library_reset), R.drawable.library_reset) { onIntent(LibraryIntent.ChangeReadingStatus(ReadingStatus.NotStarted)) }
            }
        }
    }
    if (state.sortOpen) AlertDialog(
        onDismissRequest = { if (!state.mutating) onIntent(LibraryIntent.CloseSort) }, shape = RoundedCornerShape(26.dp), containerColor = backgroundSecondary,
        title = { Text(strings.getString(R.string.library_sort), color = titleMain) },
        text = { Column {
            Text(strings.getString(R.string.library_sort_hint), style = MaterialTheme.typography.labelMedium, color = username)
            LibrarySort.entries.forEach { sort ->
                Row(Modifier.fillMaxWidth().clickable(enabled = !state.mutating) { onIntent(LibraryIntent.Sort(sort)) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(sort == state.preferences.sort, null, enabled = !state.mutating, colors = RadioButtonDefaults.colors(selectedColor = bodySecondary))
                    Text(sortText(sort), style = MaterialTheme.typography.labelMedium, color = titleMain)
                }
            }
            if (state.mutating) CircularProgressIndicator(Modifier.size(24.dp), color = bodySecondary, strokeWidth = 2.dp)
            state.sortError?.let { LibraryErrorCard(it) }
        } },
        confirmButton = {}, dismissButton = { TextButton({ onIntent(LibraryIntent.CloseSort) }, enabled = !state.mutating) { Text(strings.getString(R.string.library_cancel), color = bodySecondary) } },
    )
    state.pendingStatus?.let { change ->
        val completed = change.status == ReadingStatus.Completed
        AlertDialog(
            onDismissRequest = { if (!state.mutating) onIntent(LibraryIntent.CancelStatusChange) }, shape = RoundedCornerShape(26.dp), containerColor = backgroundSecondary,
            title = { Text(strings.getString(if (completed) R.string.library_mark_title else R.string.library_reset_title), color = titleMain) },
            text = { Column {
                Text(strings.getString(if (completed) R.string.library_mark_body else R.string.library_reset_body, change.book.title, change.book.pages), style = MaterialTheme.typography.labelMedium, color = username)
                state.operationError?.let { LibraryErrorCard(it) }
            } },
            confirmButton = { TextButton({ onIntent(LibraryIntent.ConfirmStatusChange) }, enabled = !state.mutating) {
                if (state.mutating) CircularProgressIndicator(Modifier.size(20.dp), color = bodySecondary, strokeWidth = 2.dp)
                else Text(strings.getString(if (completed) R.string.library_mark_confirm else R.string.library_reset_confirm), color = bodySecondary)
            } },
            dismissButton = { TextButton({ onIntent(LibraryIntent.CancelStatusChange) }, enabled = !state.mutating) { Text(strings.getString(R.string.library_cancel), color = username) } },
        )
    }
    if (state.helpOpen) AlertDialog(onDismissRequest = { onIntent(LibraryIntent.CloseHelp) }, shape = RoundedCornerShape(26.dp), containerColor = backgroundSecondary,
        title = { Text(strings.getString(R.string.library_help_title), color = titleMain) },
        text = { Text(strings.getString(R.string.library_help_body), Modifier.verticalScroll(rememberScrollState()), style = MaterialTheme.typography.labelMedium, color = username) },
        confirmButton = { TextButton({ onIntent(LibraryIntent.CloseHelp) }) { Text(strings.getString(R.string.library_done), color = bodySecondary) } })
}

@Composable
private fun MenuAction(text: String, icon: Int, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(painterResource(icon), null, Modifier.size(22.dp), tint = bodySecondary)
        Text(text, style = MaterialTheme.typography.labelMedium, color = titleMain)
    }
}
