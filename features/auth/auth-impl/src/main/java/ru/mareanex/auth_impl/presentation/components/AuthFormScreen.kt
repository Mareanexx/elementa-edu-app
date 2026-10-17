package ru.mareanex.auth_impl.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.mareanex.auth_impl.presentation.mvi.AuthIntent
import ru.mareanex.auth_impl.presentation.mvi.AuthUiState
import ru.mareanexx.auth_api.domain.model.AuthField
import ru.mareanexx.auth_api.domain.model.AuthMode
import ru.mareanexx.auth_impl.R
import ru.mareanexx.ui.components.buttons.ElementaMainButton
import ru.mareanexx.ui.theme.*
import ru.mareanexx.ui.utils.GlobalUiConsts.HORIZONTAL_PADDING
import ru.mareanexx.common_ui.R as CommonR

@Composable
fun AuthFormScreen(
    state: AuthUiState,
    onIntent: (AuthIntent) -> Unit,
    onBack: () -> Unit,
    onSwitchMode: () -> Unit,
) {
    val registering = state.mode == AuthMode.Register
    val loadingDescription = stringResource(R.string.auth_loading)
    val focus = LocalFocusManager.current
    BackHandler(enabled = state.isLoading) { /* Ожидаем завершения запроса перед уходом с экрана. */ }
    BoxWithConstraints(Modifier.fillMaxSize().background(authBackground).imePadding()) {
        val viewportHeight = maxHeight
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .heightIn(min = viewportHeight).padding(horizontal = HORIZONTAL_PADDING),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                IconButton(onClick = onBack, enabled = !state.isLoading, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(painterResource(CommonR.drawable.arrow_back_icon), stringResource(R.string.auth_back), tint = titleMain)
                }
                Spacer(Modifier.height(if (registering) 16.dp else 40.dp))
                Text(stringResource(if (registering) CommonR.string.screen_title_create_account else CommonR.string.screen_title_log_in),
                    style = MaterialTheme.typography.headlineLarge, color = titleMain)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(if (registering) R.string.auth_register_subtitle else R.string.auth_login_subtitle),
                    style = MaterialTheme.typography.labelMedium, color = username)
                Spacer(Modifier.height(24.dp))
                if (registering) {
                    AuthInputField(state, AuthField.FullName, state.form.fullName, R.string.auth_full_name,
                        CommonR.drawable.person_icon, onIntent, contentType = ContentType.PersonFullName)
                    Spacer(Modifier.height(14.dp))
                }
                AuthInputField(state, AuthField.Username, state.form.username, CommonR.string.username,
                    CommonR.drawable.alternate_email_icon, onIntent,
                    contentType = if (registering) ContentType.NewUsername else ContentType.Username)
                if (registering && state.errors[AuthField.Username] == null) {
                    Text(stringResource(R.string.auth_username_hint), color = username,
                        style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 12.dp, top = 6.dp))
                }
                Spacer(Modifier.height(14.dp))
                AuthInputField(state, AuthField.Password, state.form.password, CommonR.string.password,
                    CommonR.drawable.password_icon, onIntent, password = true, last = !registering,
                    contentType = if (registering) ContentType.NewPassword else ContentType.Password,
                    onSubmit = { focus.clearFocus(); onIntent(AuthIntent.Submit) })
                if (registering && state.errors[AuthField.Password] == null) {
                    Text(stringResource(R.string.auth_password_hint), color = username,
                        style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 12.dp, top = 6.dp))
                }
                if (registering) {
                    Spacer(Modifier.height(14.dp))
                    AuthInputField(state, AuthField.ConfirmPassword, state.form.confirmPassword, CommonR.string.confirm_password,
                        CommonR.drawable.password_icon, onIntent, password = true, last = true,
                        contentType = ContentType.NewPassword,
                        onSubmit = { focus.clearFocus(); onIntent(AuthIntent.Submit) })
                }
                state.failure?.let { failure ->
                    Spacer(Modifier.height(18.dp))
                    Surface(color = Color(0xFFF8E4E8), shape = MaterialTheme.shapes.medium) {
                        Row(Modifier.fillMaxWidth().padding(start = 14.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(authErrorText(failure.error)), color = Color(0xFF9F374C),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite })
                            IconButton(onClick = { onIntent(AuthIntent.DismissError) }) {
                                Icon(painterResource(CommonR.drawable.close_icon), stringResource(R.string.auth_dismiss_error),
                                    tint = Color(0xFF9F374C), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                ElementaMainButton(
                    textRes = if (registering) CommonR.string.signup_button_txt else CommonR.string.login_button_txt,
                    onClick = { focus.clearFocus(); onIntent(AuthIntent.Submit) },
                    enabled = state.canSubmit, isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth().semantics {
                        if (state.isLoading) stateDescription = loadingDescription
                    },
                )
                if (state.retryAfterSeconds > 0) {
                    Text(stringResource(R.string.auth_retry_after, state.retryAfterSeconds),
                        Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
                        color = username, style = MaterialTheme.typography.labelMedium)
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp), horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(if (registering) CommonR.string.already_have_account_question else CommonR.string.have_no_account_question),
                    color = username,
                    style = MaterialTheme.typography.displaySmall.copy(fontFamily = NormalNunito, fontWeight = FontWeight.Normal))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(if (registering) CommonR.string.login_button_txt else CommonR.string.signup_button_txt),
                    color = if (state.isLoading) bodySecondary.copy(alpha = 0.55f) else bodySecondary,
                    style = MaterialTheme.typography.displaySmall.copy(fontFamily = BoldNunito, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    modifier = Modifier
                        .clickable(enabled = !state.isLoading, role = Role.Button, onClick = onSwitchMode)
                        .heightIn(min = 48.dp).wrapContentHeight(Alignment.CenterVertically))
            }
        }
    }
}
