package ru.mareanexx.user_profile_impl.presentation.achievements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_api.domain.model.AchievementCategory
import ru.mareanexx.user_profile_impl.R
import ru.mareanexx.user_profile_impl.presentation.components.*
import ru.mareanexx.user_profile_impl.presentation.screen.components.skeleton.AchievementGridSkeleton

@Composable
internal fun AchievementsScreen(state: AchievementsUiState, onIntent: (AchievementsIntent) -> Unit, onBack: () -> Unit) {
    val strings = LocalResources.current
    val categories = remember { listOf(null) + AchievementCategory.entries }
    val pager = rememberPagerState(initialPage = categories.indexOf(state.category).coerceAtLeast(0)) { categories.size }
    val latestIntent by rememberUpdatedState(onIntent)
    LaunchedEffect(pager) {
        snapshotFlow { pager.settledPage }.distinctUntilChanged().collect { latestIntent(AchievementsIntent.SelectCategory(categories[it])) }
    }
    Scaffold(containerColor = backgroundSecondary, topBar = { ProfileTopBar(strings.getString(R.string.profile_achievements_title), onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Text(strings.getString(R.string.profile_achievements_subtitle), Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelLarge, color = username)
            Text(strings.getString(R.string.profile_achievements_count, state.items.count { it.isUnlocked }, state.items.size), Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = bodySecondary, style = MaterialTheme.typography.displaySmall)
            AchievementTabs(pager, categories)
            when {
                state.loading -> AchievementGridSkeleton()
                state.failure != null -> Box(Modifier.padding(16.dp)) { ProfileErrorBanner(state.failure.error) { onIntent(AchievementsIntent.Reload) } }
                else -> HorizontalPager(state = pager, modifier = Modifier.weight(1f).fillMaxWidth().testTag("achievements_pager"), key = { it }) { page ->
                    AchievementGrid(state, categories[page], page == pager.currentPage, onIntent)
                }
            }
        }
    }
}

@Composable
private fun AchievementGrid(state: AchievementsUiState, category: AchievementCategory?, active: Boolean, onIntent: (AchievementsIntent) -> Unit) {
    val strings = LocalResources.current
    val visible = state.items.filter { category == null || it.category == category }
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize().then(if (active) Modifier.testTag("achievements_grid") else Modifier),
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        for (unlocked in listOf(true, false)) {
            val group = visible.filter { it.isUnlocked == unlocked }
            item(span = { GridItemSpan(maxLineSpan) }, key = "section_$unlocked") {
                Text(strings.getString(R.string.profile_achievement_section_count, if (unlocked) strings.getString(R.string.profile_achievements_received) else strings.getString(R.string.profile_achievements_upcoming), group.size),
                    style = MaterialTheme.typography.titleSmall, color = titleMain, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
            }
            if (group.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                Text(if (unlocked) strings.getString(R.string.profile_achievements_received_empty) else strings.getString(R.string.profile_achievements_locked_empty), color = username, style = MaterialTheme.typography.labelMedium)
            }
            for (sectionCategory in AchievementCategory.entries) {
                val items = group.filter { it.category == sectionCategory }
                if (items.isNotEmpty()) {
                    if (category == null) item(span = { GridItemSpan(maxLineSpan) }, key = "category_${unlocked}_$sectionCategory") {
                        Text(categoryTitle(sectionCategory), color = username, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 6.dp))
                    }
                    items(items, key = { it.id }) { item ->
                        Surface(color = if (unlocked) descriptionBackground.copy(alpha = 0.35f) else authFieldBackground.copy(alpha = 0.55f), shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
                            AchievementCard(item) { onIntent(AchievementsIntent.SelectAchievement(item.id)) }
                        }
                    }
                }
            }
        }
    }
}
