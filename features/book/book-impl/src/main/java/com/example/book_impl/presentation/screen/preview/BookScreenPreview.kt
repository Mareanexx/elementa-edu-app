package com.example.book_impl.presentation.screen.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9_PRO
import androidx.compose.ui.tooling.preview.Preview
import com.example.book_impl.presentation.model.Book
import com.example.book_impl.presentation.model.BookCharacteristic
import com.example.book_impl.presentation.model.BookCharacteristics
import com.example.book_impl.presentation.model.BookReviews
import com.example.book_impl.presentation.model.BookTag
import com.example.book_impl.presentation.model.RatingStars
import com.example.book_impl.presentation.screen.BookScreen
import ru.mareanexx.ui.theme.ElementaAppTheme
import ru.mareanexx.ui.theme.bodySecondary
import ru.mareanexx.ui.theme.tagRedBackground
import ru.mareanexx.ui.theme.tagRedText
import ru.mareanexx.ui.theme.tagVioletBackground
import ru.mareanexx.common_ui.R as CommonUiR

@Preview(device = PIXEL_9_PRO)
@Composable
private fun BookScreenPreview() {
    ElementaAppTheme {
        BookScreen(
            book = Book(
                imagePath = "review_path",
                title = "Органическая химия",
                authors = "А.И. Галочкин, И.В. Ананьина",
                description = "В книге изложены номенклатура, изомерия, способы получения, применение, спектральные методы идентификации, физические и химические свойства, механизмы реакций и реакционная способность гомофункциональных производных ты будешь на берегу океана, а пока кровоточат раны",
                tags = listOf(
                    BookTag(
                        name = "Учебник",
                        textColor = bodySecondary,
                        backgroundColor = tagVioletBackground,
                    ),
                    BookTag(
                        name = "Продвинутый",
                        textColor = tagRedText,
                        backgroundColor = tagRedBackground,
                    ),
                    BookTag(
                        name = "2022",
                        textColor = bodySecondary,
                        backgroundColor = tagVioletBackground,
                    ),
                ),
                statistics = BookCharacteristics(
                    rating = BookCharacteristic(
                        mainNumber = "4.9",
                        subtitle = ">6.8 тыс. отзывов",
                        iconRes = CommonUiR.drawable.star_half_icon,
                    ),
                    size = BookCharacteristic(
                        mainNumber = "5.6 МБ",
                        subtitle = "размер",
                    ),
                    pages = BookCharacteristic(
                        mainNumber = "785",
                        subtitle = "страниц",
                    ),
                    notes = BookCharacteristic(
                        mainNumber = "12",
                        subtitle = "заметок",
                    ),
                ),
                reviews = BookReviews(
                    rating = "4.9",
                    stars = RatingStars(
                        fullyPaintedStars = 4,
                        halfPaintedStars = 1,
                        emptyStars = 0,
                    ),
                    reviewsNumber = "(6.8 тыс. отзывов)",
                    fiveStarReviewsPercent = 0.8f,
                    fourStarReviewsPercent = 0.5f,
                    threeStarReviewsPercent = 0.1f,
                    twoStarReviewsPercent = 0.06f,
                    oneStarReviewsPercent = 0.09f,
                )
            ),
        )
    }
}
