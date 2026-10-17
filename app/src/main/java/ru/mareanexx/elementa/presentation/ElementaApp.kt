package ru.mareanexx.elementa.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.*
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
import ru.mareanexx.elementa.presentation.screen.MainScreen
import ru.mareanexx.elementa.presentation.session.SessionViewModel
import ru.mareanexx.elementa.presentation.session.model.SessionState
import ru.mareanexx.elementa.presentation.session.model.SessionIntent
import ru.mareanexx.ui.theme.*
import ru.mareanexx.ui.utils.GlobalUiConsts.HORIZONTAL_PADDING

@Composable
fun ElementaApp(repository: AuthRepository) {
    val sessionViewModel: SessionViewModel = viewModel(factory = viewModelFactory { initializer { SessionViewModel(repository) } })
    val session by sessionViewModel.state.collectAsStateWithLifecycle()
    ElementaAppTheme {
        Surface(Modifier.fillMaxSize(), color = if (session is SessionState.SignedIn) backgroundMain else authBackground) {
            AnimatedContent(
                targetState = session,
                contentKey = { it.javaClass },
                modifier = Modifier.fillMaxSize().safeDrawingPadding().clipToBounds(),
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
                    SessionState.Restoring -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = bodySecondary)
                    }
                    SessionState.Guest -> AuthNavigation(repository)
                    SessionState.RestoreFailed -> Column(Modifier.fillMaxSize().padding(horizontal = HORIZONTAL_PADDING, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(stringResource(R.string.session_restore_failed), color = titleMain)
                        TextButton(onClick = { sessionViewModel.accept(SessionIntent.RetryRestore) }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                    is SessionState.SignedIn -> MainScreen(current) { sessionViewModel.accept(SessionIntent.SignOut) }
                }
            }
        }
    }
}
