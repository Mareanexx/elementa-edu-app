package com.example.book_impl.presentation.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

/**
 * Презентационная модель книги
 */
data class Book(
    val imagePath: String,
    val title: String,
    val authors: String,
    val description: String,
    val tags: List<BookTag>,
    val statistics: BookCharacteristics,
    val reviews: BookReviews,
)

/**
 * Пресентационная модель отзывов по книге
 */
data class BookReviews(
    val rating: String,
    val stars: RatingStars,
    val reviewsNumber: String,
    val fiveStarReviewsPercent: Float,
    val fourStarReviewsPercent: Float,
    val threeStarReviewsPercent: Float,
    val twoStarReviewsPercent: Float,
    val oneStarReviewsPercent: Float,
)

data class RatingStars(
    val fullyPaintedStars: Int,
    val halfPaintedStars: Int,
    val emptyStars: Int,
)

/**
 * Презентационная модель набора статистики по книге
 */
data class BookCharacteristics(
    val rating: BookCharacteristic,
    val size: BookCharacteristic,
    val pages: BookCharacteristic,
    val notes: BookCharacteristic,
)

/**
 * Презентационная модель одной характеристики книги
 */
data class BookCharacteristic(
    val mainNumber: String,
    val subtitle: String,
    @param:DrawableRes val iconRes: Int? = null,
)

/**
 * Презентационная модель тега книги (напр, "Учебник", "2022")
 */
data class BookTag(
    val name: String,
    val textColor: Color,
    val backgroundColor: Color,
)
