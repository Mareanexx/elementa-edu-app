package ru.mareanexx.user_profile_impl.presentation.achievements

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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_api.domain.model.AchievementCategory
import ru.mareanexx.user_profile_impl.R

/** Форма и цвета категорий повторяют вкладки библиотеки в макете. */
@Composable
internal fun AchievementTabs(pager: PagerState, categories: List<AchievementCategory?>) {
    val strings = LocalResources.current
    val scope = rememberCoroutineScope()
    val list = rememberLazyListState()
    val reduceMotion = LocalAppUiPreferences.current.reduceMotion
    LaunchedEffect(pager.targetPage) { list.animateScrollToItem((pager.targetPage - 1).coerceAtLeast(0)) }
    LazyRow(state = list, contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        itemsIndexed(categories) { index, category ->
            val selected = pager.targetPage == index
            val color by animateColorAsState(if (selected) bodySecondary else authFieldBackground, tween(if (reduceMotion) 0 else 180), label = "Achievement tab color")
            Box(Modifier.heightIn(min = 48.dp).selectable(selected = selected, role = Role.Tab,
                interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {
                scope.launch {
                    if (reduceMotion) pager.scrollToPage(index) else pager.animateScrollToPage(index, animationSpec = tween(320))
                }
            }), contentAlignment = Alignment.Center) {
                Surface(color = color, shape = RoundedCornerShape(50)) {
                    Text(if (category == null) strings.getString(R.string.profile_category_all) else categoryTitle(category),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (selected) androidx.compose.ui.graphics.Color.White else username,
                        style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
