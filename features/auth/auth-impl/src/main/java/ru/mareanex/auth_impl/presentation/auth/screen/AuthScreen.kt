package ru.mareanex.auth_impl.presentation.auth.screen

import androidx.compose.runtime.Composable
import ru.mareanexx.auth_api.domain.model.AuthMode
import ru.mareanex.auth_impl.presentation.components.AuthFormScreen
import ru.mareanex.auth_impl.presentation.mvi.*

@Composable
fun AuthScreen(
    onBackClick: () -> Unit,
    state: AuthUiState = AuthUiState(AuthMode.Login),
    onIntent: (AuthIntent) -> Unit = {},
    onRegisterClick: () -> Unit = {},
) = AuthFormScreen(state, onIntent, onBackClick, onRegisterClick)
