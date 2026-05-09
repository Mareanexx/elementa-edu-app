package ru.mareanexx.collections_impl.presentation.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

/**
 * Презентационная модель коллекции
 */
sealed interface Collection {
    val booksNumber: String

    data class Favorite(
        override val booksNumber: String,
        @param:StringRes val nameRes: Int,
        @param:DrawableRes val icon: Int? = null,
        val tintColor: Color? = null,
    ) : Collection

    data class UserCustom(
        override val booksNumber: String,
        val name: String,
        val image: String,
    ) : Collection
}