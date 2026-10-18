package ru.mareanexx.elementa.presentation

import ru.mareanexx.collections_api.domain.repository.CollectionsRepository
import ru.mareanexx.book_api.domain.repository.BookRepository
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ru.mareanexx.auth_api.domain.repository.AuthRepository
import ru.mareanexx.elementa.R
import ru.mareanexx.elementa.presentation.navigation.*
import ru.mareanexx.elementa.presentation.screen.SignedInContent
import ru.mareanexx.auth_api.domain.model.AuthUser
import ru.mareanexx.user_profile_api.domain.repository.ProfileRepository
import ru.mareanexx.elementa.presentation.session.SessionViewModel
import ru.mareanexx.elementa.presentation.session.model.SessionState
import ru.mareanexx.elementa.presentation.session.model.SessionIntent
import ru.mareanexx.ui.theme.*
import ru.mareanexx.ui.utils.GlobalUiConsts.HORIZONTAL_PADDING

@Composable
fun ElementaApp(repository: AuthRepository, profileRepository: (AuthUser) -> ProfileRepository, collectionsRepository: (AuthUser) -> CollectionsRepository, bookRepository: (AuthUser) -> BookRepository) {
    val sessionViewModel: SessionViewModel = viewModel(factory = viewModelFactory { initializer { SessionViewModel(repository) } })
    val session by sessionViewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val strings = androidx.compose.ui.platform.LocalResources.current
    LaunchedEffect(session is SessionState.SignedIn) {
        if (session is SessionState.SignedIn) snackbar.currentSnackbarData?.dismiss()
    }
    ElementaAppTheme {
        Box(Modifier.fillMaxSize()) {
        Surface(Modifier.fillMaxSize(), color = if (session is SessionState.SignedIn) backgroundMain else authBackground) {
            AnimatedContent(
                targetState = session,
                contentKey = { it.javaClass },
                modifier = Modifier.fillMaxSize().clipToBounds(),
                transitionSpec = {
                    when {
                        initialState is SessionState.Guest && targetState is SessionState.SignedIn ->
                            ContentTransform(pushEnter(), pushExit(), targetContentZIndex = 1f, sizeTransform = null)
                        initialState is SessionState.SignedIn && targetState is SessionState.Guest ->
                            ContentTransform(popEnter(), popExit(), targetContentZIndex = -1f, sizeTransform = null)
                        else -> ContentTransform(EnterTransition.None, ExitTransition.None, sizeTransform = null)
                    }
                },
                label = "Session navigation",
            ) { current ->
                when (current) {
                    SessionState.Restoring -> Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = bodySecondary)
                    }
                    SessionState.Guest -> Box(Modifier.fillMaxSize().safeDrawingPadding()) { AuthNavigation(repository) }
                    SessionState.RestoreFailed -> Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = HORIZONTAL_PADDING, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(stringResource(R.string.session_restore_failed), color = titleMain)
                        TextButton(onClick = { sessionViewModel.accept(SessionIntent.RetryRestore) }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                    is SessionState.SignedIn -> SignedInContent(
                        current, profileRepository, collectionsRepository, bookRepository,
                        onSessionExpired = {
                            sessionViewModel.accept(SessionIntent.SignOut)
                            scope.launch { snackbar.showSnackbar(strings.getString(R.string.session_expired)) }
                        },
                        onSignOut = { sessionViewModel.accept(SessionIntent.SignOut) },
                    )
                }
            }
        }
        if (session !is SessionState.SignedIn) {
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).safeDrawingPadding().padding(bottom = 12.dp)) {
                Snackbar(it, shape = RoundedCornerShape(16.dp))
            }
        }
        }
    }
}
