package com.example.book_impl.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.book_impl.presentation.model.Book
import com.example.book_impl.presentation.screen.components.BookCharacteristics
import com.example.book_impl.presentation.screen.components.BookDescription
import com.example.book_impl.presentation.screen.components.BookInfo
import com.example.book_impl.presentation.screen.components.BookRatingAndReviews
import ru.mareanexx.ui.components.buttons.ElementaMainButton
import ru.mareanexx.ui.components.navigation_bar.BottomNavBar
import ru.mareanexx.ui.components.navigation_bar.utils.MainNavigationTabs
import ru.mareanexx.ui.components.toolbar.SmallTitleToolbar
import ru.mareanexx.ui.components.toolbar.model.SmallTitleToolbarActionButton
import ru.mareanexx.ui.theme.addToWantToReadBackground
import ru.mareanexx.ui.theme.backgroundMain
import ru.mareanexx.ui.theme.bodySecondary
import ru.mareanexx.ui.utils.GlobalUiConsts.HORIZONTAL_PADDING
import ru.mareanexx.common_ui.R as CommonUiR

@Composable
fun BookScreen(
    book: Book,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = backgroundMain,
        topBar = {
            SmallTitleToolbar(
                title = CommonUiR.string.screen_title_about_book,
                onBackClick = { TODO() },
                actionButton = SmallTitleToolbarActionButton(
                    iconRes = CommonUiR.drawable.bookmark_icon,
                    onClick = { TODO() },
                ),
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = MainNavigationTabs.Library,
            ) { TODO() }
        },
        modifier = modifier,
    ) { topBarPaddings ->

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(topBarPaddings)
                .padding(horizontal = HORIZONTAL_PADDING),
        ) {
            BookInfo(
                book = book,
                modifier = Modifier.padding(top = 10.dp),
            )

            Spacer(modifier = Modifier.height(28.dp))

            BookCharacteristics(book.statistics)

            Spacer(modifier = Modifier.height(24.dp))

            ElementaMainButton(
                textRes = CommonUiR.string.read_button_txt,
                onClick = { TODO() },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            ElementaMainButton(
                textRes = CommonUiR.string.add_to_want_to_read,
                containerColor = addToWantToReadBackground,
                textColor = bodySecondary,
                onClick = { TODO() },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(10.dp))

            BookDescription(
                description = book.description,
                onShowFullClick = { TODO() },
            )

            Spacer(modifier = Modifier.height(18.dp))

            BookRatingAndReviews(
                reviews = book.reviews,
                onShowAllClick = { TODO() },
            )
        }
    }
}
