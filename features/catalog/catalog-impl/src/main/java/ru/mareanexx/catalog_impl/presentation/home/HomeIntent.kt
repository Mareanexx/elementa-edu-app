package ru.mareanexx.catalog_impl.presentation.home

import ru.mareanexx.catalog_api.domain.model.CatalogBook

internal sealed interface HomeIntent {
    data object Enter : HomeIntent
    data object Refresh : HomeIntent
    data class Actions(
        val book: CatalogBook,
    ) : HomeIntent
    data object CloseActions : HomeIntent
    data object ToggleFavorite : HomeIntent
    data object TogglePlanned : HomeIntent
    data object Collections : HomeIntent
    data object ClearNotice : HomeIntent
}
