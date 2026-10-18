package ru.mareanexx.book_impl.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.book_api.domain.validation.BookInputRules
import ru.mareanexx.book_impl.R
import ru.mareanexx.book_impl.presentation.BookIntent
import ru.mareanexx.book_impl.presentation.components.*
import ru.mareanexx.book_impl.presentation.model.BookUiState
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun ReviewThreadScreen(state: BookUiState, onIntent: (BookIntent) -> Unit) {
    val strings = LocalResources.current
    val thread = state.thread ?: return
    Column(Modifier.fillMaxSize().background(backgroundMain).imePadding()) {
        BookTopBar(strings.getString(R.string.book_thread_title), { onIntent(BookIntent.Back) })
        LazyColumn(Modifier.weight(1f).fillMaxWidth().testTag("book_replies_list"), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { BookReviewCard(thread.review, thread.review.id in state.votingIds, onIntent, showReply = false) }
            item { Text(strings.getString(R.string.book_replies_count, thread.totalCount), Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleSmall, color = titleMain) }
            if (thread.loading) item { ReviewSkeleton() }
            thread.failure?.let { error -> item { BookErrorCard(error) { onIntent(BookIntent.ReloadReplies) } } }
            if (thread.loaded && thread.items.isEmpty()) item {
                Column(Modifier.padding(vertical = 12.dp)) {
                    Text(strings.getString(R.string.book_thread_empty), style = MaterialTheme.typography.titleSmall, color = titleMain)
                    Text(strings.getString(R.string.book_thread_empty_hint), Modifier.padding(top = 6.dp), style = MaterialTheme.typography.labelMedium, color = username)
                }
            }
            items(thread.items, key = { it.id }) { reply ->
                var menu by remember { mutableStateOf(false) }
                Surface(shape = RoundedCornerShape(18.dp), color = if (reply.isMine) ColorMineReply else backgroundSecondary, modifier = Modifier.testTag("reply_${reply.id}")) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            ReviewerAvatar(reply.authorName, reply.avatarUrl)
                            Column(Modifier.weight(1f)) {
                                Text(if (reply.isMine) strings.getString(R.string.book_reply_mine) else reply.authorName, style = MaterialTheme.typography.displaySmall, fontSize = 14.sp, color = titleMain)
                                Text(reviewDate(reply.createdAt), style = MaterialTheme.typography.labelSmall, color = username)
                            }
                            if (reply.isMine) Box {
                                IconButton({ menu = true }) { Icon(painterResource(CommonR.drawable.more_horiz_icon), strings.getString(R.string.book_reply_more), tint = username) }
                                DropdownMenu(menu, { menu = false }, shape = RoundedCornerShape(16.dp), containerColor = backgroundSecondary) {
                                    DropdownMenuItem(text = { Text(strings.getString(R.string.book_delete)) }, onClick = { menu = false; onIntent(BookIntent.AskDeleteReply(reply.id)) })
                                }
                            }
                        }
                        Text(reply.text, Modifier.padding(top = 10.dp), style = MaterialTheme.typography.labelMedium.copy(fontFamily = NormalNunito, lineHeight = 20.sp), color = titleMain)
                    }
                }
            }
            if (thread.loadingMore) item { CircularProgressIndicator(Modifier.size(24.dp), color = bodySecondary, strokeWidth = 2.dp) }
            else if (thread.cursor != null) item { TextButton({ onIntent(BookIntent.LoadMoreReplies) }) { Text(strings.getString(R.string.book_more_replies), color = bodySecondary) } }
        }
        Surface(color = backgroundSecondary) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                thread.postError?.let { BookErrorCard(it) }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(thread.draft, { onIntent(BookIntent.ChangeReply(it)) }, Modifier.weight(1f).testTag("book_reply_input"),
                        enabled = !thread.posting && !thread.deleting, placeholder = { Text(strings.getString(R.string.book_reply_text), style = MaterialTheme.typography.labelMedium) },
                        minLines = 1, maxLines = 4, shape = RoundedCornerShape(16.dp), isError = thread.fieldError != null,
                        textStyle = MaterialTheme.typography.labelMedium.copy(color = titleMain), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = bodySecondary, unfocusedBorderColor = descriptionBackground),
                        supportingText = if (thread.fieldError != null || thread.draft.length > 800) {
                            { if (thread.fieldError != null) Text(bookErrorText(thread.fieldError)) else Text(strings.getString(R.string.book_text_counter, thread.draft.length, BookInputRules.MAX_REPLY_LENGTH)) }
                        } else null)
                    FilledIconButton({ onIntent(BookIntent.SubmitReply) }, Modifier.size(48.dp), enabled = thread.draft.isNotBlank() && !thread.posting && !thread.loading && !thread.deleting,
                        shape = RoundedCornerShape(16.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = bodySecondary, contentColor = androidx.compose.ui.graphics.Color.White)) {
                        if (thread.posting) CircularProgressIndicator(Modifier.size(20.dp), color = androidx.compose.ui.graphics.Color.White, strokeWidth = 2.dp)
                        else Icon(painterResource(R.drawable.book_send), strings.getString(R.string.book_reply_send), Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

private val ColorMineReply = androidx.compose.ui.graphics.Color(0xFFE9E5F7)
