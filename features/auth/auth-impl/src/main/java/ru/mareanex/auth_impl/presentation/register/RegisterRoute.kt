package ru.mareanex.auth_impl.presentation.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.mareanex.auth_impl.presentation.register.screen.RegisterScreen
import ru.mareanex.auth_impl.presentation.mvi.AuthViewModel
import ru.mareanex.auth_impl.presentation.mvi.AuthViewModelFactory

@Composable
fun RegisterRoute(factory: AuthViewModelFactory, onBackClick: () -> Unit, onLoginClick: () -> Unit) {
    val model: AuthViewModel = viewModel(factory = factory)
    val state by model.state.collectAsStateWithLifecycle()
    RegisterScreen(
        onBackClick = onBackClick,
        state = state,
        onIntent = model::accept,
        onLoginClick = onLoginClick
    )
}
