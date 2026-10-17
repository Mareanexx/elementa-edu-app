package ru.mareanexx.ui.components.navigation_bar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ru.mareanexx.common_ui.R
import ru.mareanexx.ui.components.navigation_bar.utils.MainNavigationTabs
import ru.mareanexx.ui.modifiers.outerShadow
import ru.mareanexx.ui.theme.LocalAppUiPreferences
import ru.mareanexx.ui.theme.bodySecondary
import ru.mareanexx.ui.theme.navBackground

@Composable
fun BottomNavBar(
    selectedTab: MainNavigationTabs,
    onTabSelected: (MainNavigationTabs) -> Unit,
) {
    val reduceMotion = LocalAppUiPreferences.current.reduceMotion
    val position by animateFloatAsState(
        targetValue = selectedTab.ordinal.toFloat(),
        animationSpec = tween(if (reduceMotion) 0 else 320, easing = FastOutSlowInEasing),
        label = "Положение выбранной вкладки",
    )
    BoxWithConstraints(
        Modifier.fillMaxWidth()
            .outerShadow(bodySecondary.copy(alpha = 0.08f), borderRadius = 28.dp, offsetY = (-2).dp, blurRadius = 12.dp)
            .background(navBackground, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .testTag("bottom_navigation"),
    ) {
        val slotWidth = (maxWidth - 32.dp) / MainNavigationTabs.entries.size
        val indicatorWidth = minOf(64.dp, slotWidth)
        val currentPosition = if (reduceMotion) selectedTab.ordinal.toFloat() else position
        // Индикатор движется под иконками, сами вкладки сохраняют своё положение и размер.
        Box(
            Modifier.offset(x = 16.dp + slotWidth * currentPosition + (slotWidth - indicatorWidth) / 2, y = 8.dp)
                .size(indicatorWidth, 36.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.verticalGradient(listOf(bodySecondary.copy(alpha = 0.16f), bodySecondary.copy(alpha = 0.08f))))
                .testTag("bottom_navigation_indicator"),
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).selectableGroup()) {
            NavigationBarItem(MainNavigationTabs.Home, R.drawable.home_icon, R.drawable.nav_home_filled, R.string.navigation_tab_home, selectedTab, onTabSelected)
            NavigationBarItem(MainNavigationTabs.Library, R.drawable.local_library_icon, R.drawable.nav_library_filled, R.string.navigation_tab_library, selectedTab, onTabSelected)
            NavigationBarItem(MainNavigationTabs.Collections, R.drawable.star_icon, R.drawable.star_filled_icon, R.string.navigation_tab_collections, selectedTab, onTabSelected)
            NavigationBarItem(MainNavigationTabs.Profile, R.drawable.person_icon, R.drawable.nav_profile_filled, R.string.navigation_tab_profile, selectedTab, onTabSelected)
        }
    }
}
