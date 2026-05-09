package ru.mareanexx.user_profile_impl.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.mareanexx.ui.components.navigation_bar.BottomNavBar
import ru.mareanexx.ui.components.navigation_bar.utils.MainNavigationTabs
import ru.mareanexx.ui.components.toolbar.MainTabsToolbar
import ru.mareanexx.ui.components.toolbar.model.ToolbarButton
import ru.mareanexx.ui.theme.backgroundMain
import ru.mareanexx.user_profile_impl.presentation.model.UserProfile
import ru.mareanexx.user_profile_impl.presentation.screen.components.Achievements
import ru.mareanexx.user_profile_impl.presentation.screen.components.MainProfileInfo
import ru.mareanexx.user_profile_impl.presentation.screen.components.ProfileStatistics
import ru.mareanexx.common_ui.R as CommonUiR

@Composable
fun UserProfileScreen(
    userData: UserProfile,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = backgroundMain,
        topBar = {
            MainTabsToolbar(
                screenTitle = CommonUiR.string.screen_title_profile,
                additionalButtons = ToolbarButton.AdditionalButton(
                    icon = CommonUiR.drawable.edit_icon,
                    onClick = { TODO() },
                ),
                settingsButton = ToolbarButton.SettingsButton(
                    icon = CommonUiR.drawable.settings_icon,
                    onClick = { TODO() },
                ),
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = MainNavigationTabs.Profile,
            ) { TODO() }
        },
        modifier = modifier,
    ) { topBarPaddings ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(topBarPaddings),
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            MainProfileInfo(userData.mainData)

            Spacer(modifier = Modifier.height(22.dp))

            ProfileStatistics(userData.statistics)

            Spacer(modifier = Modifier.height(40.dp))

            Achievements(userData.achievements)
        }
    }
}
