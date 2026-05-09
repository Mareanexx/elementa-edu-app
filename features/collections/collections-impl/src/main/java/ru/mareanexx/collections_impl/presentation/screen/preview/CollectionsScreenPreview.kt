package ru.mareanexx.collections_impl.presentation.screen.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9_PRO
import androidx.compose.ui.tooling.preview.Preview
import ru.mareanexx.collections_impl.presentation.model.Collection
import ru.mareanexx.collections_impl.presentation.screen.CollectionsScreen
import ru.mareanexx.ui.theme.ElementaAppTheme
import ru.mareanexx.common_ui.R as CommonUiR

@Preview(device = PIXEL_9_PRO)
@Composable
private fun CollectionsPreview() {
    ElementaAppTheme {
        CollectionsScreen(
            favoriteCollection = Collection.Favorite(
                nameRes = CommonUiR.string.favorite_collection_title,
                booksNumber = "12 книг",
                icon = CommonUiR.drawable.star_filled_icon,
            ),
            customCollections = listOf(
                Collection.UserCustom(
                    name = "Органическая химия",
                    booksNumber = "8 книг",
                    image = "https://picsum.photos/200",
                ),
                Collection.UserCustom(
                    name = "Научные статьи",
                    booksNumber = "14 книг",
                    image = "https://picsum.photos/200",
                ),
                Collection.UserCustom(
                    name = "Квантовая химия",
                    booksNumber = "112 книг",
                    image = "https://picsum.photos/200",
                ),
                Collection.UserCustom(
                    name = "Моя коллекция!",
                    booksNumber = "8 книг",
                    image = "https://picsum.photos/200",
                ),
                Collection.UserCustom(
                    name = "Моя коллекция!",
                    booksNumber = "8 книг",
                    image = "https://picsum.photos/200",
                ),
                Collection.UserCustom(
                    name = "Моя коллекция!",
                    booksNumber = "8 книг",
                    image = "https://picsum.photos/200",
                ),
            )
        )
    }
}
