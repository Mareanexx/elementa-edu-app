package ru.mareanexx.ui.components.navigation_bar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.ui.components.navigation_bar.utils.MainNavigationTabs
import ru.mareanexx.ui.theme.*

@Composable
internal fun RowScope.NavigationBarItem(
    tab: MainNavigationTabs,
    @DrawableRes outline: Int,
    @DrawableRes filled: Int,
    @StringRes label: Int,
    selectedTab: MainNavigationTabs,
    navigateTo: (MainNavigationTabs) -> Unit,
) {
    val selected = tab == selectedTab
    val preferences = LocalAppUiPreferences.current
    val haptics = LocalHapticFeedback.current
    val title = stringResource(label)
    val targetColor = if (selected) bodySecondary else deselectedTab
    val tint by animateColorAsState(targetColor, tween(if (preferences.reduceMotion) 0 else 220), label = "Цвет вкладки")
    val fill by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(if (preferences.reduceMotion) 0 else 280, easing = FastOutSlowInEasing),
        label = "Заполнение иконки",
    )
    Column(
        Modifier.weight(1f).selectable(
            selected = selected,
            role = Role.Tab,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                if (!selected) {
                    if (preferences.hapticFeedback) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    navigateTo(tab)
                }
            },
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().height(36.dp), contentAlignment = Alignment.Center) {
            NavigationBarIcon(outline, filled, title,
                if (preferences.reduceMotion) targetColor else tint,
                if (preferences.reduceMotion) { if (selected) 1f else 0f } else fill,
                Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(title, color = if (preferences.reduceMotion) targetColor else tint,
            fontFamily = SemiBoldNunito, fontWeight = FontWeight.Bold,
            fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.sp,
            textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
