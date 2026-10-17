package ru.mareanexx.elementa.presentation.session.model

import ru.mareanexx.auth_api.domain.model.AuthUser

sealed interface SessionState {
    data object Restoring : SessionState
    data object Guest : SessionState
    data object RestoreFailed : SessionState
    data class SignedIn(
        val user: AuthUser,
        val signingOut: Boolean = false,
        val signOutFailed: Boolean = false,
    ) : SessionState
}
