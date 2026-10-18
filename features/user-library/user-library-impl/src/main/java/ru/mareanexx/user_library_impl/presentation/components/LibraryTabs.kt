package ru.mareanexx.user_library_impl.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_impl.R
import ru.mareanexx.ui.theme.*

@Composable
internal fun LibraryTabs(pager: PagerState, counts: LibraryCounts?, enabled: Boolean, select: (LibraryCategory) -> Unit) {
    val list = rememberLazyListState()
    val reduce = LocalAppUiPreferences.current.reduceMotion
    val strings = LocalResources.current
    LaunchedEffect(pager.targetPage) {
        if (reduce) list.scrollToItem((pager.targetPage - 1).coerceAtLeast(0))
        else list.animateScrollToItem((pager.targetPage - 1).coerceAtLeast(0))
    }
    LazyRow(state = list, contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        itemsIndexed(LibraryCategory.entries) { index, category ->
            val selected = pager.targetPage == index
            val color by animateColorAsState(if (selected) bodySecondary else authFieldBackground, tween(if (reduce) 0 else 180), label = "library_tab_color")
            Box(Modifier.heightIn(min = 48.dp).testTag("library_tab_${category.name}")
                .selectable(selected, enabled = enabled, role = Role.Tab, interactionSource = remember { MutableInteractionSource() }, indication = null) { select(category) }, contentAlignment = Alignment.Center) {
                Surface(color = color, shape = RoundedCornerShape(50)) {
                    Text(if (counts == null) categoryText(category) else strings.getString(R.string.library_tab_count, categoryText(category), counts.count(category)),
                        Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = if (selected) Color.White else username, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
