package ru.mareanex.auth_impl.presentation.register.screen

import androidx.compose.runtime.Composable
import ru.mareanexx.auth_api.domain.model.AuthMode
import ru.mareanex.auth_impl.presentation.components.AuthFormScreen
import ru.mareanex.auth_impl.presentation.mvi.*

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    state: AuthUiState = AuthUiState(AuthMode.Register),
    onIntent: (AuthIntent) -> Unit = {},
    onLoginClick: () -> Unit = {},
) = AuthFormScreen(state, onIntent, onBackClick, onLoginClick)
