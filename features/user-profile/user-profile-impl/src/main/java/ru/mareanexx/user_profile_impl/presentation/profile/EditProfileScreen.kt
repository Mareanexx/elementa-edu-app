package ru.mareanexx.user_profile_impl.presentation.profile

import ru.mareanexx.user_profile_impl.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_api.domain.model.ProfileField
import ru.mareanexx.user_profile_api.domain.validation.ProfileValidator
import ru.mareanexx.user_profile_impl.presentation.components.*

@Composable
internal fun EditProfileScreen(state: ProfileUiState, onIntent: (ProfileIntent) -> Unit, onBack: () -> Unit) {
    val strings = androidx.compose.ui.platform.LocalResources.current
    var discard by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val back = { if (!state.saving) { if (state.dirty) discard = true else onBack() } }
    BackHandler { back() }
    Scaffold(containerColor = Color.White, topBar = { ProfileTopBar(strings.getString(R.string.profile_edit_title), back) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Text(strings.getString(R.string.profile_edit_heading), color = titleMain, style = MaterialTheme.typography.displayMedium)
            Text(strings.getString(R.string.profile_edit_subtitle), color = username,
                style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 10.dp, bottom = 28.dp))
            ProfileFormField(strings.getString(R.string.profile_field_name), state.form.fullName, ProfileField.FullName, state, onIntent)
            Spacer(Modifier.height(16.dp))
            ProfileFormField(strings.getString(R.string.profile_field_username), state.form.username, ProfileField.Username, state, onIntent)
            Text(strings.getString(R.string.profile_username_hint), color = username, style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 12.dp, top = 6.dp))
            Spacer(Modifier.height(20.dp))
            ProfileFormField(strings.getString(R.string.profile_field_bio), state.form.description, ProfileField.Description, state, onIntent, multiline = true)
            Text(strings.getString(R.string.profile_bio_counter, state.form.description.length, ProfileValidator.MAX_DESCRIPTION_LENGTH), color = if (state.form.description.length > ProfileValidator.MAX_DESCRIPTION_LENGTH) Color(0xFFB4485D) else username,
                style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 12.dp, top = 6.dp))
            state.failure?.let { Spacer(Modifier.height(20.dp)); ProfileErrorBanner(it.error) }
            Spacer(Modifier.height(30.dp))
            ProfilePrimaryButton(strings.getString(R.string.profile_save), state.saving, enabled = state.dirty) {
                focus.clearFocus()
                onIntent(ProfileIntent.Save)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    if (discard) AlertDialog(
        onDismissRequest = { discard = false }, containerColor = backgroundSecondary,
        title = { Text(strings.getString(R.string.profile_discard_title), color = titleMain) },
        text = { Text(strings.getString(R.string.profile_discard_body), color = username) },
        confirmButton = { TextButton(onClick = { discard = false; onBack() }) { Text(strings.getString(R.string.profile_discard), color = Color(0xFFB4485D)) } },
        dismissButton = { TextButton(onClick = { discard = false }) { Text(strings.getString(R.string.profile_continue), color = bodySecondary) } },
    )
}

@Composable
private fun ProfileFormField(label: String, value: String, field: ProfileField, state: ProfileUiState, onIntent: (ProfileIntent) -> Unit, multiline: Boolean = false) {
    val strings = androidx.compose.ui.platform.LocalResources.current
    var focused by remember { mutableStateOf(false) }
    val error = state.errors[field]?.let { strings.getString(profileErrorResource(it)) }
    Text(label, style = MaterialTheme.typography.displaySmall, color = titleMain, modifier = Modifier.padding(bottom = 8.dp))
    val fieldModifier = Modifier.fillMaxWidth().testTag("profile_field_${field.name}").onFocusChanged {
        if (focused && !it.isFocused) onIntent(ProfileIntent.BlurField(field))
        focused = it.isFocused
    }
    if (multiline) {
        ProfileBioField(value, label, !state.saving, focused, error, fieldModifier) { onIntent(ProfileIntent.ChangeField(field, it)) }
        return
    }
    OutlinedTextField(
        value = value, onValueChange = { onIntent(ProfileIntent.ChangeField(field, it)) }, enabled = !state.saving,
        modifier = fieldModifier,
        placeholder = { Text(label, color = username) },
        singleLine = true, maxLines = 1,
        shape = RoundedCornerShape(18.dp), textStyle = MaterialTheme.typography.labelLarge,
        isError = error != null,
        supportingText = error?.let { { Text(it, color = Color(0xFFB4485D), style = MaterialTheme.typography.labelSmall) } },
        keyboardOptions = KeyboardOptions(
            capitalization = if (field == ProfileField.Username) KeyboardCapitalization.None else KeyboardCapitalization.Sentences,
            autoCorrectEnabled = field != ProfileField.Username,
            keyboardType = if (field == ProfileField.Username) KeyboardType.Ascii else KeyboardType.Text,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = authFieldBackground, unfocusedContainerColor = authFieldBackground, disabledContainerColor = authFieldBackground,
            errorContainerColor = authFieldBackground, unfocusedBorderColor = Color.Transparent, focusedBorderColor = bodySecondary,
            focusedTextColor = titleMain, unfocusedTextColor = titleMain, errorTextColor = titleMain,
            errorBorderColor = Color(0xFFB4485D), cursorColor = bodySecondary,
        ),
    )
}
