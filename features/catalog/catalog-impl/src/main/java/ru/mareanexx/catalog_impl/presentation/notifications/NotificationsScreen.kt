package ru.mareanexx.catalog_impl.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_impl.R
import ru.mareanexx.catalog_impl.presentation.components.*
import ru.mareanexx.catalog_impl.presentation.model.NotificationUiState
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationsScreen(state: NotificationUiState, intent: (NotificationsIntent) -> Unit, onBack: () -> Unit) {
    val list = rememberLazyListState()
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) { while (true) { delay(60_000); now = Instant.now() } }
    LaunchedEffect(state.filter) { list.scrollToItem(0) }
    LaunchedEffect(list, state.nextCursor, state.loadingMore, state.appendFailed, state.items.size) {
        if (state.nextCursor != null && !state.loadingMore && !state.appendFailed) snapshotFlow { list.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }.distinctUntilChanged().collect { if (it >= state.items.size - 2) intent(NotificationsIntent.More) }
    }
    Column(Modifier.fillMaxSize().testTag("notifications_screen")) {
        CatalogTopBar(stringResource(R.string.catalog_notifications), onBack)
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.catalog_unread_badge, state.unreadCount), Modifier.weight(1f), color = username, style = MaterialTheme.typography.labelMedium)
            TextButton({ intent(NotificationsIntent.MarkAll) }, enabled = state.loaded && state.unreadCount > 0 && !state.saving) { Text(stringResource(R.string.notifications_mark_all), color = if (state.unreadCount > 0) bodySecondary else username, style = MaterialTheme.typography.labelMedium) }
        }
        CatalogTabs(listOf(stringResource(R.string.notifications_all), stringResource(R.string.notifications_unread)), state.filter.ordinal, enabled = !state.saving) { intent(NotificationsIntent.Filter(NotificationFilter.entries[it])) }
        state.writeError?.let { CatalogErrorBanner(it, { intent(NotificationsIntent.RetryWrite) }, Modifier.padding(16.dp)) }
        PullToRefreshBox(state.refreshing, { intent(NotificationsIntent.Refresh) }, Modifier.weight(1f), indicator = {}) {
            LazyColumn(Modifier.fillMaxSize().testTag("notifications_list"), state = list, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.error?.let { item { CatalogErrorBanner(it, { intent(NotificationsIntent.Retry) }) } }
                if (state.loading) item { CatalogSkeleton() }
                if (state.loaded && state.items.isEmpty()) item { CatalogEmpty(stringResource(if (state.filter == NotificationFilter.Unread) R.string.notifications_unread_empty else R.string.notifications_empty_title), stringResource(R.string.notifications_empty_body), icon = CommonR.drawable.notifications_icon) }
                items(state.items, key = { it.id }) { note -> NotificationCard(note, now, !state.saving) { intent(NotificationsIntent.Open(note)) } }
                if (state.loadingMore || state.saving) item { Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(22.dp), color = bodySecondary, strokeWidth = 2.dp) } }
            }
        }
    }
}
@Composable
private fun NotificationCard(note: CatalogNotification, now: Instant, enabled: Boolean, onClick: () -> Unit) {
    val unread = note.readAt == null
    val description = stringResource(R.string.notifications_new)
    Surface(Modifier.fillMaxWidth().testTag("notification_${note.id}").then(if (unread) Modifier.semantics { stateDescription = description } else Modifier), color = if (unread) backgroundSecondary else backgroundSecondary.copy(alpha = .65f), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.catalogClick(enabled, onClick).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(color = if (unread) tagVioletBackground else authFieldBackground, shape = RoundedCornerShape(14.dp)) { Icon(painterResource(if (note.bookId != null) CommonR.drawable.menu_book_icon else CommonR.drawable.public_icon), null, Modifier.padding(10.dp).size(23.dp), tint = if (unread) bodySecondary else username) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(notificationTime(note.createdAt, now), Modifier.weight(1f), color = username, fontFamily = SemiBoldNunito, fontSize = 11.sp)
                    if (unread) Box(Modifier.size(7.dp).background(bodySecondary, CircleShape))
                }
                Text(note.title, color = titleMain, fontFamily = ExtraBoldNunito, fontSize = 16.sp, lineHeight = 21.sp)
                Text(note.body, color = username, fontFamily = SemiBoldNunito, fontSize = 13.sp, lineHeight = 19.sp)
                if (note.bookId != null || note.categoryId != null) Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(stringResource(if (note.bookId != null) R.string.notifications_open_book else R.string.notifications_open_category), color = bodySecondary, fontFamily = BoldNunito, fontSize = 12.sp)
                    Icon(painterResource(R.drawable.catalog_arrow), null, Modifier.size(15.dp), tint = bodySecondary)
                }
            }
        }
    }
}
