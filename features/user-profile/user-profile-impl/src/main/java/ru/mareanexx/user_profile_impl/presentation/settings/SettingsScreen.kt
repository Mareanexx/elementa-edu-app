package ru.mareanexx.user_profile_impl.presentation.settings

import ru.mareanexx.user_profile_impl.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_impl.presentation.components.*

@Composable
internal fun SettingsScreen(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    signingOut: Boolean,
    signOutFailed: Boolean,
    onSignOut: () -> Unit,
    onBack: () -> Unit,
) {
    val strings = androidx.compose.ui.platform.LocalResources.current
    var signOutDialog by rememberSaveable { mutableStateOf(false) }
    var cacheDialog by rememberSaveable { mutableStateOf(false) }
    var textScale by remember { mutableFloatStateOf(state.settings.textScale) }
    LaunchedEffect(state.settings.textScale, state.saving) { if (!state.saving) textScale = state.settings.textScale }
    val enabled = !state.loading && !state.saving && !signingOut
    val context = LocalContext.current
    val version = remember(context) { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0" }
    BackHandler(enabled = state.saving || signingOut) {}
    Scaffold(containerColor = backgroundMain, topBar = { ProfileTopBar(strings.getString(R.string.profile_settings_title), { if (!state.saving && !signingOut) onBack() }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(Color(0xFFDEDAFF), Color(0xFFF7F5FF))), RoundedCornerShape(24.dp),
            ).padding(20.dp)) {
                Text(strings.getString(R.string.profile_settings_heading), style = MaterialTheme.typography.displayMedium, color = titleMain)
                Text(strings.getString(R.string.profile_settings_subtitle), style = MaterialTheme.typography.labelLarge,
                    color = username, modifier = Modifier.padding(top = 8.dp))
            }
            if (state.loading || state.saving) LinearProgressIndicator(Modifier.fillMaxWidth(), color = bodySecondary, trackColor = descriptionBackground)
            state.failure?.let { ProfileErrorBanner(it.error) { onIntent(SettingsIntent.Reload) } }
            Text(strings.getString(R.string.profile_settings_accessibility), style = MaterialTheme.typography.labelSmall, color = username, modifier = Modifier.padding(top = 8.dp))
            SettingsCard {
                Text(strings.getString(R.string.profile_settings_text_size), style = MaterialTheme.typography.displaySmall, color = titleMain)
                Text(strings.getString(R.string.profile_settings_text_size_hint), style = MaterialTheme.typography.labelMedium, color = username,
                    modifier = Modifier.padding(top = 6.dp))
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(strings.getString(R.string.profile_settings_font_sample), color = username)
                    Text(strings.getString(R.string.profile_percentage, (textScale * 100).roundToInt()), color = bodySecondary, style = MaterialTheme.typography.displaySmall)
                    Text(strings.getString(R.string.profile_settings_font_sample), color = titleMain, fontSize = 23.sp)
                }
                Slider(modifier = Modifier.testTag("profile_text_scale"), value = textScale, onValueChange = { textScale = it }, valueRange = 0.9f..1.3f, steps = 3, enabled = enabled,
                    onValueChangeFinished = { onIntent(SettingsIntent.Update(state.settings.copy(textScale = (textScale * 10).roundToInt() / 10f))) },
                    colors = SliderDefaults.colors(thumbColor = bodySecondary, activeTrackColor = bodySecondary))
                Text(strings.getString(R.string.profile_settings_preview), color = titleMain,
                    fontSize = (18f * textScale / state.settings.textScale).sp, modifier = Modifier.padding(vertical = 10.dp))
            }
            SettingsCard {
                SettingsToggle(strings.getString(R.string.profile_settings_reduce_motion), strings.getString(R.string.profile_settings_reduce_motion_hint), state.settings.reduceMotion, enabled) {
                    onIntent(SettingsIntent.Update(state.settings.copy(reduceMotion = it)))
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = descriptionBackground)
                SettingsToggle(strings.getString(R.string.profile_settings_keep_screen_on), strings.getString(R.string.profile_settings_keep_screen_on_hint), state.settings.keepScreenOn, enabled) {
                    onIntent(SettingsIntent.Update(state.settings.copy(keepScreenOn = it)))
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = descriptionBackground)
                SettingsToggle(strings.getString(R.string.profile_settings_haptics), strings.getString(R.string.profile_settings_haptics_hint), state.settings.hapticFeedback, enabled) {
                    onIntent(SettingsIntent.Update(state.settings.copy(hapticFeedback = it)))
                }
            }
            Text(strings.getString(R.string.profile_settings_storage), style = MaterialTheme.typography.labelSmall, color = username, modifier = Modifier.padding(top = 8.dp))
            SettingsCard {
                Text(strings.getString(R.string.profile_settings_cache), style = MaterialTheme.typography.displaySmall, color = titleMain)
                Text(strings.getString(R.string.profile_settings_cache_hint),
                    style = MaterialTheme.typography.labelMedium, color = username, modifier = Modifier.padding(top = 8.dp))
                TextButton(onClick = { cacheDialog = true }, enabled = enabled, contentPadding = PaddingValues(top = 12.dp)) {
                    Text(strings.getString(R.string.profile_settings_clear_cache), color = bodySecondary, style = MaterialTheme.typography.displaySmall)
                }
            }
            Text(strings.getString(R.string.profile_settings_about), style = MaterialTheme.typography.labelSmall, color = username, modifier = Modifier.padding(top = 8.dp))
            SettingsCard {
                Text(strings.getString(R.string.profile_app_name), style = MaterialTheme.typography.titleSmall, color = titleMain)
                Text(strings.getString(R.string.profile_settings_version, version), style = MaterialTheme.typography.labelMedium, color = username,
                    modifier = Modifier.padding(top = 6.dp))
            }
            state.notice?.let { notice ->
                Surface(color = descriptionBackground, shape = RoundedCornerShape(16.dp)) {
                    Text(strings.getString(if (notice == SettingsNotice.Saved) R.string.profile_notice_saved else R.string.profile_notice_cache_cleared), Modifier.padding(16.dp), color = titleMain, style = MaterialTheme.typography.labelMedium)
                }
            }
            if (signOutFailed) Text(strings.getString(R.string.profile_settings_logout_error), color = Color(0xFFB4485D))
            OutlinedButton(onClick = { signOutDialog = true }, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB4485D))) {
                if (signingOut) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text(strings.getString(R.string.profile_settings_logout), style = MaterialTheme.typography.displaySmall)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
    if (signOutDialog) AlertDialog(
        onDismissRequest = { signOutDialog = false }, containerColor = backgroundSecondary,
        title = { Text(strings.getString(R.string.profile_settings_logout_title), color = titleMain) },
        text = { Text(strings.getString(R.string.profile_settings_logout_body), color = username) },
        confirmButton = { TextButton(onClick = { signOutDialog = false; onSignOut() }) { Text(strings.getString(R.string.profile_logout), color = Color(0xFFB4485D)) } },
        dismissButton = { TextButton(onClick = { signOutDialog = false }) { Text(strings.getString(R.string.profile_stay), color = bodySecondary) } },
    )
    if (cacheDialog) AlertDialog(
        onDismissRequest = { cacheDialog = false }, containerColor = backgroundSecondary,
        title = { Text(strings.getString(R.string.profile_settings_clear_title), color = titleMain) },
        text = { Text(strings.getString(R.string.profile_settings_clear_body), color = username) },
        confirmButton = { TextButton(onClick = { cacheDialog = false; onIntent(SettingsIntent.ClearImageCache) }) { Text(strings.getString(R.string.profile_clear), color = bodySecondary) } },
        dismissButton = { TextButton(onClick = { cacheDialog = false }) { Text(strings.getString(R.string.profile_cancel), color = username) } },
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(color = backgroundSecondary, shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), content = content)
    }
}

@Composable
private fun SettingsToggle(title: String, description: String, checked: Boolean, enabled: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.displaySmall, color = titleMain)
            Text(description, style = MaterialTheme.typography.labelMedium, color = username, modifier = Modifier.padding(top = 6.dp))
        }
        Switch(checked, onCheckedChange = onChecked, enabled = enabled,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = bodySecondary),
            modifier = Modifier.semantics { contentDescription = title })
    }
}
