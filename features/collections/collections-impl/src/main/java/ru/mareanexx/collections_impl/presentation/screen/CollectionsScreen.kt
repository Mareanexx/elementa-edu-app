package ru.mareanexx.collections_impl.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.mareanexx.collections_impl.presentation.model.Collection
import ru.mareanexx.collections_impl.presentation.screen.components.BigCollectionCard
import ru.mareanexx.collections_impl.presentation.screen.components.SmallCollectionCardsGrid
import ru.mareanexx.ui.components.content_header.BlockContentHeader
import ru.mareanexx.ui.components.navigation_bar.BottomNavBar
import ru.mareanexx.ui.components.navigation_bar.utils.MainNavigationTabs
import ru.mareanexx.ui.components.toolbar.MainTabsToolbar
import ru.mareanexx.ui.components.toolbar.model.ToolbarButton
import ru.mareanexx.ui.theme.backgroundMain
import ru.mareanexx.common_ui.R as CommonUiR

@Composable
fun CollectionsScreen(
    favoriteCollection: Collection.Favorite,
    customCollections: List<Collection.UserCustom>,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = backgroundMain,
        topBar = {
            MainTabsToolbar(
                screenTitle = CommonUiR.string.screen_title_collections,
                additionalButtons = ToolbarButton.AdditionalButton(
                    icon = CommonUiR.drawable.add_icon,
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
                selectedTab = MainNavigationTabs.Collections,
            ) { TODO() }
        },
        modifier = modifier,
    ) { topBarPaddings ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(topBarPaddings),
        ) {
            BigCollectionCard(
                collection = favoriteCollection,
                modifier = Modifier.padding(top = 16.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            BlockContentHeader(
                titleRes = CommonUiR.string.block_all_collections_header,
                buttonText = CommonUiR.string.show_all,
                buttonIcon = CommonUiR.drawable.keyboard_arrow_right_icon,
                onButtonClick = { TODO() },
            )

            SmallCollectionCardsGrid(
                collections = customCollections,
            )
        }
    }
}