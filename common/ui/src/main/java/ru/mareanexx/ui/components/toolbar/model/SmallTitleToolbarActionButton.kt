package ru.mareanexx.ui.components.toolbar.model

import androidx.annotation.DrawableRes

/**
 * Модель данных для экшн кнопки в небольшом тулбаре
 */
data class SmallTitleToolbarActionButton(
    @param:DrawableRes val iconRes: Int,
    val onClick: () -> Unit,
)
