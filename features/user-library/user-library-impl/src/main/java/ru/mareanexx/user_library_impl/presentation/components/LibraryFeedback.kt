package ru.mareanexx.user_library_impl.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_impl.R
import ru.mareanexx.user_library_impl.presentation.LibraryIntent
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR
import com.valentinilk.shimmer.shimmer
import ru.mareanexx.ui.components.skeleton.BoxSkeleton

@Composable
internal fun LibraryErrorCard(error: LibraryError, onRetry: (() -> Unit)? = null) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFF8E9EE)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(libraryError(error), color = Color(0xFF985568), style = MaterialTheme.typography.labelMedium)
            if (onRetry != null) TextButton(onRetry, contentPadding = PaddingValues(0.dp)) { Text(LocalResources.current.getString(R.string.library_retry), color = bodySecondary) }
        }
    }
}

@Composable
internal fun LibraryEmpty(category: LibraryCategory, searching: Boolean, onAll: () -> Unit) {
    val strings = LocalResources.current
    val title = if (searching) R.string.library_search_empty else when (category) {
        LibraryCategory.All -> R.string.library_empty_title
        LibraryCategory.InProgress -> R.string.library_empty_progress
        LibraryCategory.Completed -> R.string.library_empty_completed
        LibraryCategory.WantToRead -> R.string.library_empty_want
        LibraryCategory.Downloaded -> R.string.library_empty_downloaded
    }
    val hint = if (searching) R.string.library_search_empty_hint else when (category) {
        LibraryCategory.All -> R.string.library_empty_hint
        LibraryCategory.InProgress -> R.string.library_empty_progress_hint
        LibraryCategory.Completed -> R.string.library_empty_completed_hint
        LibraryCategory.WantToRead -> R.string.library_empty_want_hint
        LibraryCategory.Downloaded -> R.string.library_empty_downloaded_hint
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp).testTag("library_empty"), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(26.dp), color = tagVioletBackground.copy(alpha = .5f)) {
            Icon(painterResource(CommonR.drawable.menu_book_icon), null, Modifier.padding(22.dp).size(44.dp), tint = deselectedTab)
        }
        Text(strings.getString(title), Modifier.padding(top = 20.dp), style = MaterialTheme.typography.titleSmall, color = titleMain, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(strings.getString(hint), Modifier.padding(top = 10.dp), style = MaterialTheme.typography.labelMedium.copy(fontFamily = NormalNunito), color = username, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (category != LibraryCategory.All) TextButton(onAll, Modifier.padding(top = 12.dp)) { Text(strings.getString(if (searching) R.string.library_search_other else R.string.library_browse_all), color = bodySecondary) }
    }
}

@Composable
internal fun LibrarySkeleton() {
    Column(Modifier.fillMaxWidth().padding(16.dp).testTag("library_skeleton").then(if (LocalAppUiPreferences.current.reduceMotion) Modifier else Modifier.shimmer()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BoxSkeleton(modifier = Modifier.width(180.dp).height(23.dp))
        repeat(3) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                BoxSkeleton(width = 110, height = 168)
                Column(Modifier.weight(1f).padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BoxSkeleton(modifier = Modifier.fillMaxWidth().height(24.dp))
                    BoxSkeleton(modifier = Modifier.fillMaxWidth(.7f).height(18.dp))
                    Spacer(Modifier.height(40.dp))
                    BoxSkeleton(modifier = Modifier.fillMaxWidth().height(14.dp))
                }
            }
        }
    }
}
