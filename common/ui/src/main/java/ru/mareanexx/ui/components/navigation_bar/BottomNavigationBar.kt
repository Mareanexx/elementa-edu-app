package ru.mareanexx.ui.components.navigation_bar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.common_ui.R
import ru.mareanexx.ui.modifiers.outerShadow
import ru.mareanexx.ui.components.navigation_bar.utils.MainNavigationTabs
import ru.mareanexx.ui.components.navigation_bar.utils.MainNavigationTabs.Collections
import ru.mareanexx.ui.components.navigation_bar.utils.MainNavigationTabs.Home
import ru.mareanexx.ui.components.navigation_bar.utils.MainNavigationTabs.Library
import ru.mareanexx.ui.components.navigation_bar.utils.MainNavigationTabs.Profile
import ru.mareanexx.ui.theme.SemiBoldNunito
import ru.mareanexx.ui.theme.deselectedTab
import ru.mareanexx.ui.theme.navBackground
import ru.mareanexx.ui.theme.outerShadow
import ru.mareanexx.ui.theme.selectedTab

@Composable
fun BottomNavBar(
    selectedTab: MainNavigationTabs,
    onTabSelected: (MainNavigationTabs) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .outerShadow(outerShadow, offsetY = (-2).dp, blurRadius = 10.dp)
            .background(
                color = navBackground,
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            )
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        NavItem(R.drawable.home_icon, R.string.navigation_tab_home, selectedTab == Home) {
            onTabSelected(Home)
        }
        NavItem(R.drawable.local_library_icon, R.string.navigation_tab_library, selectedTab == Library) {
            onTabSelected(Library)
        }
        NavItem(R.drawable.star_icon, R.string.navigation_tab_collections, selectedTab == Collections) {
            onTabSelected(Collections)
        }
        NavItem(R.drawable.person_icon, R.string.navigation_tab_profile, selectedTab == Profile) {
            onTabSelected(Profile)
        }
    }
}

@Composable
fun NavItem(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    selected: Boolean,
    navigateTo: () -> Unit,
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(selected) {
        if (selected) {
            scale.snapTo(1f)
            scale.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(durationMillis = 100, easing = LinearOutSlowInEasing),
            )
            scale.animateTo(
                targetValue = 1.05f,
                animationSpec = tween(durationMillis = 100, easing = FastOutLinearInEasing),
            )
        }
    }

    Column(
        modifier = Modifier
            .widthIn(min = 86.dp)
            .padding(vertical = 5.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { navigateTo() }
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier.size(28.dp),
            painter = painterResource(icon),
            contentDescription = stringResource(label),
            tint = if (selected) selectedTab else deselectedTab,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(label),
            color = if (selected) selectedTab else deselectedTab,
            fontFamily = SemiBoldNunito,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            letterSpacing = 0.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
