package ru.mareanex.auth_impl.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.mareanexx.auth_api.domain.model.AuthMode
import ru.mareanexx.auth_api.domain.repository.AuthRepository
import ru.mareanexx.auth_api.domain.usecase.LoginUseCase
import ru.mareanexx.auth_api.domain.usecase.RegisterUseCase

class AuthViewModelFactory(
    private val repository: AuthRepository,
    private val mode: AuthMode,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AuthViewModel::class.java))
        return AuthViewModel(LoginUseCase(repository), RegisterUseCase(repository), mode) as T
    }
}
