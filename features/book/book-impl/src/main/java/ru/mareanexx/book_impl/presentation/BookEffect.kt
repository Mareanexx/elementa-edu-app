package ru.mareanexx.book_impl.presentation

sealed interface BookEffect {
    data object Close : BookEffect
}
