package ru.mareanexx.ui.components.navigation_bar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.mareanexx.ui.components.navigation_bar.utils.MainNavigationTabs
import ru.mareanexx.ui.theme.ElementaAppTheme

@Preview(
    name = "Выбранный таб `Главная`",
)
@Composable
private fun HomeTabBottomNavigationBarPreview() {
    ElementaAppTheme {
        BottomNavBar(
            selectedTab = MainNavigationTabs.Home,
            onTabSelected = {},
        )
    }
}

@Preview(
    name = "Выбранный таб `Библиотека`",
)
@Composable
private fun LibraryTabBottomNavigationBarPreview() {
    ElementaAppTheme {
        BottomNavBar(
            selectedTab = MainNavigationTabs.Library,
            onTabSelected = {},
        )
    }
}

@Preview(
    name = "Выбранный таб `Коллекции`",
)
@Composable
private fun CollectionsTabBottomNavigationBarPreview() {
    ElementaAppTheme {
        BottomNavBar(
            selectedTab = MainNavigationTabs.Collections,
            onTabSelected = {},
        )
    }
}

@Preview(
    name = "Выбранный таб `Профиль`",
)
@Composable
private fun ProfileTabBottomNavigationBarPreview() {
    ElementaAppTheme {
        BottomNavBar(
            selectedTab = MainNavigationTabs.Profile,
            onTabSelected = {},
        )
    }
}