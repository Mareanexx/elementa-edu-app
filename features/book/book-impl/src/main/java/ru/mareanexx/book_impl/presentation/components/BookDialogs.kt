package ru.mareanexx.book_impl.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.mareanexx.book_api.domain.model.BookError
import ru.mareanexx.book_api.domain.validation.BookInputRules
import ru.mareanexx.book_impl.R
import ru.mareanexx.book_impl.presentation.BookIntent
import ru.mareanexx.book_impl.presentation.model.*
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun BookDialogs(state: BookUiState, onIntent: (BookIntent) -> Unit) {
    val strings = LocalResources.current
    val maxHeight = LocalConfiguration.current.screenHeightDp.dp * .85f
    if (state.descriptionOpen) Dialog({ onIntent(BookIntent.DismissDescription) }) {
        Surface(shape = RoundedCornerShape(26.dp), color = backgroundSecondary) {
            Column(Modifier.fillMaxWidth().heightIn(max = maxHeight).padding(20.dp)) {
                DialogHeader(strings.getString(R.string.book_description), { onIntent(BookIntent.DismissDescription) })
                SelectionContainer(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).testTag("book_full_description")) {
                    Text(state.book?.description?.ifBlank { strings.getString(R.string.book_no_description) }.orEmpty(), style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp, lineHeight = 22.sp), color = titleMain)
                }
                BookPrimaryButton(strings.getString(R.string.book_done), { onIntent(BookIntent.DismissDescription) }, Modifier.fillMaxWidth().padding(top = 16.dp))
            }
        }
    }
    state.reviewDraft?.let { draft ->
        Dialog({ if (!draft.saving) onIntent(BookIntent.CloseReviewEditor) }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(Modifier.padding(16.dp).widthIn(max = 520.dp).fillMaxWidth().heightIn(max = maxHeight).imePadding(), shape = RoundedCornerShape(26.dp), color = backgroundSecondary) {
                Column(Modifier.padding(20.dp)) {
                    Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                        DialogHeader(strings.getString(R.string.book_review_editor_title), { onIntent(BookIntent.CloseReviewEditor) }, enabled = !draft.saving)
                        Text(strings.getString(R.string.book_review_editor_hint), style = MaterialTheme.typography.labelMedium, color = username)
                        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center) {
                            for (stars in 1..5) IconButton({ onIntent(BookIntent.ChangeReviewRating(stars)) }, enabled = !draft.saving, modifier = Modifier.size(48.dp).testTag("book_rate_$stars")) {
                                Icon(painterResource(if (draft.rating >= stars) CommonR.drawable.star_filled_icon else CommonR.drawable.star_icon), strings.getString(R.string.book_review_rating, stars), Modifier.size(34.dp), tint = rating)
                            }
                        }
                        if (draft.fieldError == BookError.RatingRequired) Text(bookErrorText(draft.fieldError), color = androidx.compose.ui.graphics.Color(0xFFAD5268), style = MaterialTheme.typography.labelSmall)
                        OutlinedTextField(draft.text, { onIntent(BookIntent.ChangeReviewText(it)) }, Modifier.fillMaxWidth().testTag("book_review_input"), enabled = !draft.saving,
                            label = { Text(strings.getString(R.string.book_review_text)) }, placeholder = { Text(strings.getString(R.string.book_review_text_hint), style = MaterialTheme.typography.labelMedium) },
                            minLines = 4, maxLines = 7, shape = RoundedCornerShape(16.dp), textStyle = MaterialTheme.typography.labelMedium.copy(color = titleMain),
                            isError = draft.fieldError == BookError.ReviewTooLong,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = bodySecondary, unfocusedBorderColor = descriptionBackground),
                            supportingText = { Row(Modifier.fillMaxWidth()) {
                                Text(strings.getString(R.string.book_review_optional), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
                                Text(strings.getString(R.string.book_text_counter, draft.text.length, BookInputRules.MAX_REVIEW_LENGTH), style = MaterialTheme.typography.labelSmall)
                            } })
                        draft.requestError?.let { BookErrorCard(it) }
                    }
                    BookPrimaryButton(strings.getString(if (state.book?.myReview == null) R.string.book_review_publish else R.string.book_review_submit_edit),
                        { onIntent(BookIntent.SaveReview) }, Modifier.fillMaxWidth().padding(top = 12.dp), loading = draft.saving)
                }
            }
        }
    }
    if (state.reviewDeleteOpen) AlertDialog(
        onDismissRequest = { if (!state.deletingReview) onIntent(BookIntent.CancelDeleteReview) }, shape = RoundedCornerShape(26.dp), containerColor = backgroundSecondary,
        title = { Text(strings.getString(R.string.book_review_delete_title), color = titleMain) },
        text = { Column { Text(strings.getString(R.string.book_review_delete_body)); state.operationError?.let { BookErrorCard(it) } } },
        confirmButton = { TextButton({ onIntent(BookIntent.ConfirmDeleteReview) }, enabled = !state.deletingReview) { Text(strings.getString(R.string.book_delete), color = androidx.compose.ui.graphics.Color(0xFFAD5268)) } },
        dismissButton = { TextButton({ onIntent(BookIntent.CancelDeleteReview) }, enabled = !state.deletingReview) { Text(strings.getString(R.string.book_cancel)) } },
    )
    state.thread?.takeIf { it.deleteId != null }?.let { thread -> AlertDialog(
        onDismissRequest = { if (!thread.deleting) onIntent(BookIntent.CancelDeleteReply) }, shape = RoundedCornerShape(26.dp), containerColor = backgroundSecondary,
        title = { Text(strings.getString(R.string.book_reply_delete_title), color = titleMain) },
        text = { Column { Text(strings.getString(R.string.book_reply_delete_body)); thread.postError?.let { BookErrorCard(it) } } },
        confirmButton = { TextButton({ onIntent(BookIntent.ConfirmDeleteReply) }, enabled = !thread.deleting) { Text(strings.getString(R.string.book_delete), color = androidx.compose.ui.graphics.Color(0xFFAD5268)) } },
        dismissButton = { TextButton({ onIntent(BookIntent.CancelDeleteReply) }, enabled = !thread.deleting) { Text(strings.getString(R.string.book_cancel)) } },
    ) }
    state.collections?.let { BookCollectionsSheet(it, onIntent) }
}

@Composable
private fun DialogHeader(title: String, onClose: () -> Unit, enabled: Boolean = true) {
    val strings = LocalResources.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = titleMain)
        IconButton(onClose, enabled = enabled) { Icon(painterResource(CommonR.drawable.close_icon), strings.getString(R.string.book_close), tint = username) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookCollectionsSheet(state: BookCollectionsState, onIntent: (BookIntent) -> Unit) {
    val strings = LocalResources.current
    val latestBusy by rememberUpdatedState(state.saving)
    val canChange = remember { { _: SheetValue -> !latestBusy } }
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = canChange)
    val height = (LocalConfiguration.current.screenHeightDp.dp * .72f).coerceAtMost(560.dp)
    ModalBottomSheet({ if (!state.saving) onIntent(BookIntent.CloseCollections) }, sheetState = sheet, containerColor = backgroundSecondary,
        dragHandle = { Box(Modifier.padding(top = 8.dp, bottom = 4.dp).size(32.dp, 4.dp).background(descriptionBackground, RoundedCornerShape(2.dp))) }) {
        Column(Modifier.fillMaxWidth().height(height).padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
            DialogHeader(strings.getString(R.string.book_collections_title), { onIntent(BookIntent.CloseCollections) }, enabled = !state.saving)
            Text(strings.getString(R.string.book_collections_hint), style = MaterialTheme.typography.labelMedium, color = username)
            LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (state.loading) item { ReviewSkeleton() }
                state.failure?.let { error -> item { BookErrorCard(error) {
                    onIntent(if (state.items.isEmpty() || error == BookError.CollectionNotFound || error == BookError.Conflict) BookIntent.ReloadCollections else BookIntent.SaveCollections)
                } } }
                if (!state.loading && state.items.isEmpty() && state.failure == null) item {
                    Column(Modifier.padding(vertical = 24.dp)) {
                        Text(strings.getString(R.string.book_collections_empty), style = MaterialTheme.typography.titleSmall, color = titleMain)
                        Text(strings.getString(R.string.book_collections_empty_hint), Modifier.padding(top = 8.dp), style = MaterialTheme.typography.labelMedium, color = username)
                    }
                }
                items(state.items, key = { it.id }) { item ->
                    Row(Modifier.fillMaxWidth().toggleable(item.id in state.selected, enabled = !state.saving, role = Role.Checkbox, onValueChange = { onIntent(BookIntent.ToggleCollection(item.id)) }).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(CommonR.drawable.local_library_icon), null, Modifier.padding(end = 12.dp).size(28.dp), tint = bodySecondary)
                        Column(Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.displaySmall, color = titleMain)
                            Text(strings.getString(R.string.book_collection_count, item.bookCount), style = MaterialTheme.typography.labelSmall, color = username)
                        }
                        Checkbox(item.id in state.selected, null, enabled = !state.saving, colors = CheckboxDefaults.colors(checkedColor = bodySecondary))
                    }
                }
            }
            BookPrimaryButton(strings.getString(R.string.book_save), { onIntent(BookIntent.SaveCollections) }, Modifier.fillMaxWidth(),
                enabled = !state.loading && state.items.isNotEmpty(), loading = state.saving)
        }
    }
}
