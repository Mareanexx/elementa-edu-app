package ru.mareanexx.elementa.presentation.session.model

sealed interface SessionIntent {
    data object RetryRestore : SessionIntent
    data object SignOut : SessionIntent
}
