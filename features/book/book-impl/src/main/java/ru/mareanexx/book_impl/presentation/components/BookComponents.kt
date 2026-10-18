package ru.mareanexx.book_impl.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.mareanexx.book_api.domain.model.BookError
import ru.mareanexx.book_impl.R
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun BookTopBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    val strings = LocalResources.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onBack, Modifier.offset(x = (-8).dp)) { Icon(painterResource(CommonR.drawable.arrow_back_icon), strings.getString(R.string.book_back), tint = titleMain) }
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.headlineLarge, color = titleMain)
        actions()
    }
}

@Composable
internal fun BookPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, loading: Boolean = false) {
    Button(onClick, modifier.heightIn(min = 52.dp), enabled = enabled && !loading, shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bodySecondary, contentColor = Color.White)) {
        if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        else Text(text, style = MaterialTheme.typography.displaySmall)
    }
}

@Composable
internal fun BookSectionHeader(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, color = titleMain)
        if (action != null) TextButton(onAction, contentPadding = PaddingValues(start = 8.dp)) {
            Text(action, style = MaterialTheme.typography.labelMedium, color = bodySecondary)
            Icon(painterResource(CommonR.drawable.keyboard_arrow_right_icon), null, Modifier.size(18.dp), tint = bodySecondary)
        }
    }
}

@Composable
internal fun BookErrorCard(error: BookError, onRetry: (() -> Unit)? = null) {
    val strings = LocalResources.current
    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFF8E7EC)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text(bookErrorText(error), style = MaterialTheme.typography.labelMedium, color = Color(0xFFA54D68))
            if (onRetry != null) TextButton(onRetry, contentPadding = PaddingValues(0.dp)) { Text(strings.getString(R.string.book_retry), color = bodySecondary) }
        }
    }
}

@Composable
internal fun bookErrorText(error: BookError): String = LocalResources.current.getString(when (error) {
    BookError.Offline -> R.string.book_error_offline
    BookError.Timeout -> R.string.book_error_timeout
    BookError.Unauthorized -> R.string.book_error_session
    BookError.NotFound -> R.string.book_error_not_found
    BookError.Unavailable -> R.string.book_error_unavailable
    BookError.InvalidResponse -> R.string.book_error_response
    BookError.RateLimited -> R.string.book_error_rate
    BookError.Forbidden -> R.string.book_error_forbidden
    BookError.Conflict -> R.string.book_error_conflict
    BookError.RatingRequired -> R.string.book_error_rating
    BookError.ReviewTooLong -> R.string.book_error_review_length
    BookError.ReplyRequired -> R.string.book_error_reply_required
    BookError.ReplyTooLong -> R.string.book_error_reply_length
    BookError.CollectionNotFound -> R.string.book_error_collection
    BookError.Storage -> R.string.book_error_storage
})
