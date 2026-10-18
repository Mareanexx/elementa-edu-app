package ru.mareanexx.catalog_impl.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.dp
import ru.mareanexx.catalog_impl.R
import ru.mareanexx.catalog_impl.presentation.home.HomeIntent
import ru.mareanexx.catalog_impl.presentation.model.HomeUiState
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogBookActions(state: HomeUiState, intent: (HomeIntent) -> Unit) {
    val book = state.actionBook ?: return
    ModalBottomSheet(onDismissRequest = { intent(HomeIntent.CloseActions) }, containerColor = backgroundSecondary, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = {
            Box(Modifier.fillMaxWidth().height(20.dp).testTag("catalog_actions_handle"), contentAlignment = Alignment.Center) {
                Box(Modifier.size(width = 30.dp, height = 3.dp).background(divider, RoundedCornerShape(2.dp)))
            }
        }) {
        Column(Modifier.fillMaxWidth().testTag("catalog_book_actions").padding(horizontal = 16.dp).padding(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.catalog_actions_title), color = titleMain, style = MaterialTheme.typography.titleSmall)
            Text(book.title, color = username, style = MaterialTheme.typography.labelMedium)
            state.actionError?.let { Text(errorText(it), color = androidx.compose.ui.graphics.Color(0xFF9D4F62), style = MaterialTheme.typography.labelMedium) }
            ActionRow(stringResource(if (book.shelves.favorite) R.string.catalog_favorite_remove else R.string.catalog_favorite_add), R.drawable.catalog_heart, !state.saving) { intent(HomeIntent.ToggleFavorite) }
            ActionRow(stringResource(if (book.shelves.wantToRead) R.string.catalog_planned_remove else R.string.catalog_planned_add), CommonR.drawable.bookmark_icon, !state.saving) { intent(HomeIntent.TogglePlanned) }
            ActionRow(stringResource(R.string.catalog_collections_add), CommonR.drawable.star_icon, !state.saving) { intent(HomeIntent.Collections) }
            if (state.saving) LinearProgressIndicator(Modifier.fillMaxWidth(), color = bodySecondary)
        }
    }
}
@Composable
private fun ActionRow(title: String, icon: Int, enabled: Boolean, onClick: () -> Unit) {
    Surface(color = authFieldBackground.copy(alpha = .6f), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().catalogClick(enabled, onClick).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(icon), null, Modifier.size(22.dp), tint = bodySecondary)
            Text(title, color = titleMain, style = MaterialTheme.typography.displaySmall)
        }
    }
}
