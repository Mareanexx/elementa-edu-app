package ru.mareanexx.collections_impl.presentation

sealed interface CollectionsEffect {
    data class OpenBook(
        val id: String,
    ) : CollectionsEffect
}
