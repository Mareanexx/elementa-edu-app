package ru.mareanexx.catalog_impl.presentation.search

internal sealed interface SearchIntent {
    data object Enter : SearchIntent
    data class Edit(
        val value: String,
    ) : SearchIntent
    data class ChooseQuery(
        val value: String,
    ) : SearchIntent
    data class RemoveQuery(
        val value: String,
    ) : SearchIntent
    data object Submit : SearchIntent
    data object ClearHistory : SearchIntent
    data object More : SearchIntent
    data object Retry : SearchIntent
    data object Remember : SearchIntent
}
