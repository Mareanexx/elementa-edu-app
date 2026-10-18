package ru.mareanex.auth_impl.presentation.welcome.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.auth_impl.R
import ru.mareanexx.ui.components.buttons.ElementaMainButton
import ru.mareanexx.ui.theme.*
import ru.mareanexx.ui.utils.GlobalUiConsts.HORIZONTAL_PADDING
import ru.mareanexx.common_ui.R as CommonR

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
) {
    Column(modifier.fillMaxSize().background(authBackground).verticalScroll(rememberScrollState()).padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Spacer(Modifier.height(12.dp))
        Image(painterResource(R.drawable.welcome_chemistry), null, contentScale = ContentScale.Fit,
            alignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(340.dp))
        Text(stringResource(CommonR.string.welcome_screen_heading, stringResource(CommonR.string.app_name), stringResource(CommonR.string.welcome_screen_title)),
            style = MaterialTheme.typography.displayLarge, fontSize = 36.sp, lineHeight = 38.sp,
            color = titleMain, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING))
        Text(stringResource(CommonR.string.welcome_screen_subtitle), style = MaterialTheme.typography.labelLarge,
            color = username, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING))
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth().padding(horizontal = HORIZONTAL_PADDING), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ElementaMainButton(CommonR.string.signup_button_txt, onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth().height(60.dp))
            ElementaMainButton(CommonR.string.login_button_txt, containerColor = addToWantToReadBackground,
                textColor = bodySecondary, onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth().height(60.dp))
        }
    }
}
