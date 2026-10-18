package ru.mareanexx.collections_impl.presentation.components

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
import ru.mareanexx.collections_api.domain.model.*
import ru.mareanexx.collections_impl.R
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun CollectionsToolbar(title: String, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    val strings = LocalResources.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) IconButton(onBack, Modifier.offset(x = (-8).dp)) { Icon(painterResource(CommonR.drawable.arrow_back_icon), strings.getString(R.string.collections_back), tint = titleMain) }
        Text(title, Modifier.weight(1f), style = SecondaryScreenTitle, color = titleMain, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, maxLines = 2)
        actions()
    }
}

@Composable
internal fun CollectionsPrimaryButton(text: String, enabled: Boolean = true, loading: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick, enabled = enabled && !loading, modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = bodySecondary, disabledContainerColor = if (loading) bodySecondary else authFieldBackground)) {
        if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        else Text(text, color = if (enabled) Color.White else username, style = MaterialTheme.typography.displaySmall)
    }
}

@Composable
internal fun collectionsError(error: CollectionsError): String {
    val strings = LocalResources.current
    return strings.getString(when (error) {
        CollectionsError.InvalidImage -> R.string.collections_error_image
        CollectionsError.NameRequired -> R.string.collections_error_name_required
        CollectionsError.NameTooLong -> R.string.collections_error_name_long
        CollectionsError.NameTaken -> R.string.collections_error_name_taken
        CollectionsError.LimitReached -> R.string.collections_error_limit
        CollectionsError.NotFound -> R.string.collections_error_not_found
        CollectionsError.Protected -> R.string.collections_error_protected
        CollectionsError.Offline -> R.string.collections_error_offline
        CollectionsError.Timeout -> R.string.collections_error_timeout
        CollectionsError.Unauthorized -> R.string.collections_error_unauthorized
        CollectionsError.Unavailable -> R.string.collections_error_unavailable
        CollectionsError.InvalidResponse -> R.string.collections_error_response
        CollectionsError.Storage -> R.string.collections_error_storage
        CollectionsError.RateLimited -> R.string.collections_error_rate
        CollectionsError.InvalidBooks -> R.string.collections_error_books
    })
}

@Composable
internal fun CollectionsErrorCard(error: CollectionsError, retry: (() -> Unit)? = null) {
    val strings = LocalResources.current
    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFF8E4E8)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(collectionsError(error), color = Color(0xFF9F374C), style = MaterialTheme.typography.labelMedium)
            if (retry != null) TextButton(retry) { Text(strings.getString(R.string.collections_retry), color = bodySecondary) }
        }
    }
}

@Composable
internal fun collectionTitle(item: BookCollection): String = when {
    item.isFavorite -> LocalResources.current.getString(R.string.collections_favorite)
    item.isReadingList -> LocalResources.current.getString(R.string.collections_reading_list)
    else -> item.name
}

@Composable
internal fun bookCount(number: Int): String {
    val resource = when {
        number % 100 in 11..14 -> R.string.collections_books_many
        number % 10 == 1 -> R.string.collections_books_one
        number % 10 in 2..4 -> R.string.collections_books_few
        else -> R.string.collections_books_many
    }
    return LocalResources.current.getString(resource, number)
}

@Composable
internal fun sortTitle(sort: CollectionSort): String = LocalResources.current.getString(when (sort) {
    CollectionSort.NameAscending -> R.string.collections_sort_name_asc
    CollectionSort.NameDescending -> R.string.collections_sort_name_desc
    CollectionSort.BooksDescending -> R.string.collections_sort_books_desc
    CollectionSort.BooksAscending -> R.string.collections_sort_books_asc
})
