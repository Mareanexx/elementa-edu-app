package ru.mareanex.auth_impl.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.*
import ru.mareanex.auth_impl.presentation.mvi.AuthIntent
import ru.mareanex.auth_impl.presentation.mvi.AuthUiState
import ru.mareanexx.auth_api.domain.model.AuthField
import ru.mareanexx.auth_impl.R
import ru.mareanexx.ui.components.text_fields.ElementaTextField
import ru.mareanexx.ui.theme.authFieldBackground
import ru.mareanexx.ui.theme.username
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun AuthInputField(
    state: AuthUiState, field: AuthField, value: String, label: Int, icon: Int,
    onIntent: (AuthIntent) -> Unit, password: Boolean = false, last: Boolean = false,
    contentType: ContentType, onSubmit: () -> Unit = {},
) {
    var hadFocus by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val confirmation = field == AuthField.ConfirmPassword
    val visible = if (confirmation) state.confirmPasswordVisible else state.passwordVisible
    ElementaTextField(
        value = value, onValueChange = { onIntent(AuthIntent.FieldChanged(field, it)) },
        leadingIcon = icon, placeholderText = label, enabled = !state.isLoading,
        containerColor = authFieldBackground,
        unfocusedBorderColor = Color.Transparent,
        error = state.errors[field]?.let { stringResource(authErrorText(it)) },
        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            capitalization = if (field == AuthField.FullName) KeyboardCapitalization.Words else KeyboardCapitalization.None,
            autoCorrectEnabled = field == AuthField.FullName,
            keyboardType = if (password) KeyboardType.Password else if (field == AuthField.Username) KeyboardType.Ascii else KeyboardType.Text,
            imeAction = if (last) ImeAction.Done else ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(onNext = { focus.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) },
            onDone = { onSubmit() }),
        trailingIcon = {
            if (password) IconButton(onClick = { onIntent(AuthIntent.TogglePassword(confirmation)) }, enabled = !state.isLoading) {
                Icon(painterResource(if (visible) CommonR.drawable.visibility_off_icon else CommonR.drawable.visibility_icon),
                    stringResource(if (visible) R.string.auth_hide_password else R.string.auth_show_password), tint = username)
            }
        },
        modifier = Modifier.fillMaxWidth().semantics { this.contentType = contentType }
            .onFocusChanged {
                if (hadFocus && !it.isFocused) onIntent(AuthIntent.FieldBlurred(field))
                hadFocus = it.isFocused
            },
    )
}
