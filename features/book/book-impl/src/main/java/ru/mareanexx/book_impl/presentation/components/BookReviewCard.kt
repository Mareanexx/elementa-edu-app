package ru.mareanexx.book_impl.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_impl.R
import ru.mareanexx.book_impl.presentation.BookIntent
import ru.mareanexx.ui.theme.*
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun BookReviewCard(review: BookReview, voting: Boolean, onIntent: (BookIntent) -> Unit, preview: Boolean = false, showReply: Boolean = true) {
    val strings = LocalResources.current
    var menu by remember { mutableStateOf(false) }
    var expanded by rememberSaveable(review.id) { mutableStateOf(false) }
    var overflow by remember { mutableStateOf(false) }
    Surface(shape = RoundedCornerShape(22.dp), color = backgroundSecondary, modifier = Modifier.testTag("review_${review.id}")) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReviewerAvatar(review.authorName, review.avatarUrl)
                Column(Modifier.weight(1f)) {
                    Text(if (review.isMine) strings.getString(R.string.book_my_review) else review.authorName, style = MaterialTheme.typography.displaySmall, fontSize = 14.sp, color = titleMain, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(reviewDate(review.createdAt), Modifier.padding(top = 3.dp), style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, color = username)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFF1D8)) {
                    Row(Modifier.padding(horizontal = 7.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(painterResource(CommonR.drawable.star_filled_icon), null, Modifier.size(13.dp), tint = rating)
                        Text(review.rating.toString(), style = MaterialTheme.typography.labelSmall, color = Color(0xFF92662D))
                    }
                }
                if (review.isMine) Box {
                    IconButton({ menu = true }, Modifier.size(36.dp)) { Icon(painterResource(CommonR.drawable.more_horiz_icon), strings.getString(R.string.book_review_more), tint = username) }
                    DropdownMenu(menu, { menu = false }, shape = RoundedCornerShape(16.dp), containerColor = backgroundSecondary) {
                        DropdownMenuItem(text = { Text(strings.getString(R.string.book_review_edit)) }, onClick = { menu = false; onIntent(BookIntent.OpenReviewEditor) })
                        DropdownMenuItem(text = { Text(strings.getString(R.string.book_delete), color = Color(0xFFAD5268)) }, onClick = { menu = false; onIntent(BookIntent.AskDeleteReview) })
                    }
                }
            }
            Text(review.text.ifBlank { strings.getString(R.string.book_review_empty_text) }, Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = NormalNunito, lineHeight = 20.sp), color = if (review.text.isBlank()) username else titleMain,
                maxLines = if (expanded) Int.MAX_VALUE else if (preview) 4 else 6, overflow = TextOverflow.Ellipsis,
                onTextLayout = { if (!expanded) overflow = it.hasVisualOverflow })
            if (overflow || expanded) TextButton({ expanded = !expanded }, contentPadding = PaddingValues(0.dp)) {
                Text(strings.getString(if (expanded) R.string.book_collapse else R.string.book_full), color = bodySecondary, style = MaterialTheme.typography.labelSmall)
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReactionButton(review.likes, R.drawable.book_thumb_up, strings.getString(R.string.book_like), review.reaction == ReviewReaction.Like, !voting && !review.isMine,
                    Modifier.testTag("review_like_${review.id}")) { onIntent(BookIntent.React(review.id, ReviewReaction.Like)) }
                ReactionButton(review.dislikes, R.drawable.book_thumb_down, strings.getString(R.string.book_dislike), review.reaction == ReviewReaction.Dislike, !voting && !review.isMine,
                    Modifier.testTag("review_dislike_${review.id}")) { onIntent(BookIntent.React(review.id, ReviewReaction.Dislike)) }
                Spacer(Modifier.weight(1f))
                if (showReply) TextButton({ onIntent(BookIntent.OpenThread(review.id)) }, contentPadding = PaddingValues(0.dp)) {
                    Icon(painterResource(R.drawable.book_reply), null, Modifier.size(16.dp), tint = bodySecondary)
                    Spacer(Modifier.width(5.dp))
                    Text(if (review.replyCount == 0) strings.getString(R.string.book_reply_action) else strings.getString(R.string.book_replies_count, review.replyCount),
                        style = MaterialTheme.typography.labelSmall, color = bodySecondary)
                }
            }
        }
    }
}

@Composable
private fun ReactionButton(count: Int, icon: Int, description: String, selected: Boolean, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val strings = LocalResources.current
    Surface(shape = RoundedCornerShape(12.dp), color = if (selected) tagVioletBackground else authFieldBackground.copy(alpha = .45f),
        modifier = modifier.semantics { contentDescription = description; this.selected = selected; if (selected) stateDescription = strings.getString(R.string.book_reaction_selected) }
            .clickable(enabled = enabled, interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Button, onClick = onClick)) {
        Row(Modifier.heightIn(min = 44.dp).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(painterResource(icon), null, Modifier.size(17.dp), tint = if (selected) bodySecondary else username)
            Text(count.toString(), style = MaterialTheme.typography.labelSmall, color = if (selected) bodySecondary else username)
        }
    }
}

@Composable
internal fun ReviewerAvatar(name: String, url: String?) {
    val initials = remember(name) { name.split(Regex("\\s+")).mapNotNull { it.firstOrNull(Char::isLetter)?.uppercaseChar() }.take(2).joinToString("") }
    Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEDE8F7)), contentAlignment = Alignment.Center) {
        Text(initials, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8270AF))
        if (url != null) AsyncImage(url, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}

internal fun reviewDate(timestamp: Long): String = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.forLanguageTag("ru")).format(Date(timestamp))
