package ru.mareanexx.catalog_impl.presentation

import ru.mareanexx.book_api.domain.model.BookEntry

internal sealed interface CatalogEffect {
    data object SessionExpired : CatalogEffect
    data class OpenBook(
        val id: String,
        val entry: BookEntry = BookEntry.Details,
    ) : CatalogEffect
    data class OpenCategory(
        val id: String,
    ) : CatalogEffect
}
