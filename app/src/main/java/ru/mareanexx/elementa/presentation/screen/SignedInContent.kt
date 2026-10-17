package ru.mareanexx.elementa.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.mareanexx.auth_api.domain.model.AuthUser
import ru.mareanexx.elementa.R
import ru.mareanexx.elementa.presentation.session.model.SessionState
import ru.mareanexx.ui.components.navigation_bar.BottomNavBar
import ru.mareanexx.ui.components.navigation_bar.utils.MainNavigationTabs
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_api.domain.model.AppSettings
import ru.mareanexx.user_profile_api.domain.repository.ProfileRepository
import ru.mareanexx.user_profile_impl.presentation.ProfileRoute

@Composable
internal fun SignedInContent(
    state: SessionState.SignedIn,
    createRepository: (AuthUser) -> ProfileRepository,
    onSessionExpired: () -> Unit,
    onSignOut: () -> Unit,
) {
    val repository = remember(state.user.id) { createRepository(state.user) }
    val settings by repository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    var tab by rememberSaveable(state.user.id) { mutableStateOf(MainNavigationTabs.Home) }
    var showBottomBar by rememberSaveable { mutableStateOf(true) }
    var profileScreenColor by remember { mutableStateOf(backgroundMain) }
    val density = LocalDensity.current
    val view = LocalView.current
    val strings = LocalResources.current
    DisposableEffect(view, settings.keepScreenOn) {
        val previous = view.keepScreenOn
        view.keepScreenOn = settings.keepScreenOn
        onDispose { view.keepScreenOn = previous }
    }
    BackHandler(enabled = tab != MainNavigationTabs.Home && showBottomBar) { tab = MainNavigationTabs.Home }
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * settings.textScale),
        LocalAppUiPreferences provides AppUiPreferences(settings.reduceMotion, settings.hapticFeedback),
    ) {
        val screenColor = if (tab == MainNavigationTabs.Profile) profileScreenColor else backgroundMain
        Box(Modifier.fillMaxSize().background(screenColor)) {
            Scaffold(
                modifier = Modifier.safeDrawingPadding(),
                containerColor = backgroundMain,
                bottomBar = {
                    AnimatedVisibility(
                        visible = showBottomBar || tab != MainNavigationTabs.Profile,
                        enter = expandVertically(tween(if (settings.reduceMotion) 0 else 460), expandFrom = Alignment.Bottom),
                        exit = shrinkVertically(tween(if (settings.reduceMotion) 0 else 460), shrinkTowards = Alignment.Bottom),
                    ) {
                        BottomNavBar(tab) { selected ->
                            tab = selected
                            showBottomBar = true
                        }
                    }
                },
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (tab) {
                        MainNavigationTabs.Home -> MainScreen(state, onSignOut)
                        MainNavigationTabs.Profile -> ProfileRoute(
                            repository, state.user.id, state.signingOut, state.signOutFailed, onSignOut,
                            onSessionExpired = onSessionExpired,
                            onRootScreenChanged = { showBottomBar = it },
                            onScreenColorChanged = { profileScreenColor = it },
                        )
                        MainNavigationTabs.Library -> EmptySection(strings.getString(R.string.library_title), strings.getString(R.string.library_empty))
                        MainNavigationTabs.Collections -> EmptySection(strings.getString(R.string.collections_title), strings.getString(R.string.collections_empty))
                    }
                }
            }
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().windowInsetsBottomHeight(WindowInsets.safeDrawing)
                .background(if (showBottomBar || tab != MainNavigationTabs.Profile) navBackground else screenColor))
        }
    }
}

@Composable
private fun EmptySection(title: String, description: String) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge, color = titleMain, modifier = Modifier.padding(vertical = 16.dp))
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(description, color = username, style = MaterialTheme.typography.labelLarge)
        }
    }
}
