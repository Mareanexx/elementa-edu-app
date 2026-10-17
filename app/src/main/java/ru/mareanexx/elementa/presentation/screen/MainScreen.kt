package ru.mareanexx.elementa.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.mareanexx.elementa.R
import ru.mareanexx.elementa.presentation.session.model.SessionState
import ru.mareanexx.ui.theme.*
import ru.mareanexx.ui.utils.GlobalUiConsts.HORIZONTAL_PADDING

@Composable
internal fun MainScreen(state: SessionState.SignedIn, onSignOut: () -> Unit) {
    Column(Modifier.fillMaxSize().background(backgroundMain).padding(horizontal = HORIZONTAL_PADDING, vertical = 20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.main_title), style = MaterialTheme.typography.headlineLarge, color = titleMain)
            TextButton(onClick = onSignOut, enabled = !state.signingOut) {
                if (state.signingOut) CircularProgressIndicator(Modifier.size(20.dp), color = bodySecondary, strokeWidth = 2.dp)
                else Text(stringResource(R.string.sign_out), color = bodySecondary)
            }
        }
        if (state.signOutFailed) Text(stringResource(R.string.sign_out_failed), color = MaterialTheme.colorScheme.error)
    }
}
