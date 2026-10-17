package ru.mareanex.auth_impl.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.mareanex.auth_impl.presentation.auth.screen.AuthScreen
import ru.mareanex.auth_impl.presentation.mvi.AuthViewModel
import ru.mareanex.auth_impl.presentation.mvi.AuthViewModelFactory

@Composable
fun AuthRoute(factory: AuthViewModelFactory, onBackClick: () -> Unit, onRegisterClick: () -> Unit) {
    val model: AuthViewModel = viewModel(factory = factory)
    val state by model.state.collectAsStateWithLifecycle()
    AuthScreen(onBackClick = onBackClick, state = state, onIntent = model::accept, onRegisterClick = onRegisterClick)
}
