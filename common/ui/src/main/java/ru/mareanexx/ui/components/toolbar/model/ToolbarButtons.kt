package ru.mareanexx.ui.components.toolbar.model

import androidx.annotation.DrawableRes

sealed interface ToolbarButton {
    /**
     * Дополнительная кнопка, зависит от экрана.
     * Расположена перед кнопкой настроек
     */
    data class AdditionalButton(
        @param:DrawableRes val icon: Int,
        val onClick: () -> Unit,
    ) : ToolbarButton

    /**
     * Кнопка открывающая настройки экрана
     */
    data class SettingsButton(
        @param:DrawableRes val icon: Int,
        val onClick: () -> Unit,
    ) : ToolbarButton
}