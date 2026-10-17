package ru.mareanexx.elementa.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.mareanex.auth_impl.presentation.auth.AuthRoute
import ru.mareanex.auth_impl.presentation.register.RegisterRoute
import ru.mareanex.auth_impl.presentation.welcome.screen.WelcomeScreen
import ru.mareanex.auth_impl.presentation.mvi.AuthViewModelFactory
import ru.mareanexx.auth_api.domain.model.AuthMode
import ru.mareanexx.auth_api.domain.repository.AuthRepository

@Composable
internal fun AuthNavigation(repository: AuthRepository) {
    val navigation = rememberNavController()
    fun open(route: String) {
        navigation.navigate(route) { popUpTo("welcome"); launchSingleTop = true }
    }
    NavHost(
        navigation, startDestination = "welcome", modifier = Modifier.fillMaxSize(),
        enterTransition = { pushEnter() }, exitTransition = { pushExit() },
        popEnterTransition = { popEnter() }, popExitTransition = { popExit() },
    ) {
        composable("welcome") {
            WelcomeScreen(onLoginClick = { open("login") }, onRegisterClick = { open("register") })
        }
        composable("login") {
            AuthRoute(AuthViewModelFactory(repository, AuthMode.Login),
                onBackClick = { navigation.popBackStack() }, onRegisterClick = { open("register") })
        }
        composable("register") {
            RegisterRoute(AuthViewModelFactory(repository, AuthMode.Register),
                onBackClick = { navigation.popBackStack() }, onLoginClick = { open("login") })
        }
    }
}
