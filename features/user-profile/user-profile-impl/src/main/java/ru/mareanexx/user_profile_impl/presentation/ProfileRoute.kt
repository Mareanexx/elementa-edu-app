package ru.mareanexx.user_profile_impl.presentation

import ru.mareanexx.ui.components.feedback.SyncIndicator
import androidx.compose.ui.Alignment
import ru.mareanexx.user_profile_impl.R
import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.mareanexx.user_profile_impl.presentation.navigation.ProfileDestination
import androidx.compose.ui.draw.clipToBounds
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_api.domain.model.ProfileError
import ru.mareanexx.user_profile_api.domain.repository.ProfileRepository
import ru.mareanexx.user_profile_impl.presentation.achievements.*
import ru.mareanexx.user_profile_impl.presentation.photo.*
import ru.mareanexx.user_profile_impl.presentation.profile.*
import ru.mareanexx.user_profile_impl.presentation.screen.UserProfileScreen
import ru.mareanexx.user_profile_impl.presentation.settings.*

@Composable
fun ProfileRoute(
    repository: ProfileRepository,
    userId: String,
    signingOut: Boolean,
    signOutFailed: Boolean,
    onSignOut: () -> Unit,
    onSessionExpired: () -> Unit,
    onRootScreenChanged: (Boolean) -> Unit,
    onScreenColorChanged: (Color) -> Unit,
) {
    val strings = androidx.compose.ui.platform.LocalResources.current
    val factory = remember(repository) { ProfileViewModelFactory(repository) }
    val profile: ProfileViewModel = viewModel(key = "profile_$userId", factory = factory)
    val achievements: AchievementsViewModel = viewModel(key = "achievements_$userId", factory = factory)
    val settings: SettingsViewModel = viewModel(key = "settings_$userId", factory = factory)
    val state by profile.state.collectAsStateWithLifecycle()
    val achievementState by achievements.state.collectAsStateWithLifecycle()
    val settingsState by settings.state.collectAsStateWithLifecycle()
    var destination by rememberSaveable { mutableStateOf(ProfileDestination.Overview) }
    val context = LocalContext.current
    val processor = remember(context) { AvatarImageProcessor(context) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val preferences = LocalAppUiPreferences.current
    val latestHapticsEnabled by rememberUpdatedState(preferences.hapticFeedback)
    val haptics = LocalHapticFeedback.current
    var sourceSheet by rememberSaveable { mutableStateOf(false) }
    var removeDialog by rememberSaveable { mutableStateOf(false) }
    var draft by rememberSaveable { mutableStateOf<String?>(null) }
    var cameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    var importing by remember { mutableStateOf(false) }

    fun navigate(route: ProfileDestination) {
        if (!state.saving && !state.avatarBusy && !importing && destination != route) {
            destination = route
        }
    }
    fun closeEditor() {
        destination = ProfileDestination.Overview
    }
    fun importPhoto(uri: Uri) {
        sourceSheet = false
        importing = true
        scope.launch {
            try {
                val copied = withContext(Dispatchers.IO) { processor.copyToDraft(uri) }
                processor.discard(draft)
                draft = copied
                profile.accept(ProfileIntent.DismissError)
                destination = ProfileDestination.Avatar
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                snackbar.showSnackbar(strings.getString(R.string.profile_photo_import_error))
            } finally { importing = false }
        }
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let(::importPhoto) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { importPhoto(Uri.parse(it)) }
        cameraUri = null
    }

    LaunchedEffect(destination) {
        onRootScreenChanged(destination == ProfileDestination.Overview)
        onScreenColorChanged(when (destination) {
            ProfileDestination.Overview, ProfileDestination.Settings -> backgroundMain
            ProfileDestination.Edit -> Color.White
            else -> backgroundSecondary
        })
        if (destination != ProfileDestination.Avatar && draft != null) {
            if (!preferences.reduceMotion) delay(360)
            processor.discard(draft)
            draft = null
        }
    }
    LaunchedEffect(repository) {
        profile.accept(ProfileIntent.Reload)
        achievements.accept(AchievementsIntent.Reload)
        settings.accept(SettingsIntent.Reload)
    }
    LaunchedEffect(state.failure, achievementState.failure, settingsState.failure) {
        if (listOf(profile.state.value.failure, achievements.state.value.failure, settings.state.value.failure).any { it?.error == ProfileError.Unauthorized }) onSessionExpired()
    }
    LaunchedEffect(profile) {
        profile.effects.collect { effect ->
            when (effect) {
                ProfileEffect.Saved -> { destination = ProfileDestination.Overview; scope.launch { snackbar.showSnackbar(strings.getString(R.string.profile_saved)) } }
                ProfileEffect.AvatarUpdated -> {
                    if (destination == ProfileDestination.Avatar) closeEditor()
                    if (latestHapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    scope.launch { snackbar.showSnackbar(strings.getString(R.string.profile_photo_saved)) }
                }
            }
        }
    }
    BackHandler(enabled = destination != ProfileDestination.Overview) {
        if (!state.avatarBusy && !state.saving && !importing) destination = ProfileDestination.Overview
    }

    Scaffold(containerColor = Color.Transparent, snackbarHost = { SnackbarHost(snackbar) { data -> Snackbar(data, shape = RoundedCornerShape(16.dp)) } }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
        AnimatedContent(
            targetState = if (destination == ProfileDestination.Achievements) ProfileDestination.Overview else destination,
            modifier = Modifier.fillMaxSize().clipToBounds(),
            transitionSpec = {
                val forward = initialState == ProfileDestination.Overview
                val enter = when {
                    preferences.reduceMotion -> EnterTransition.None
                    forward -> slideInHorizontally(tween(360)) { it }
                    else -> slideInHorizontally(tween(360)) { -it / 3 }
                }
                val exit = when {
                    preferences.reduceMotion -> ExitTransition.None
                    forward -> slideOutHorizontally(tween(360)) { -it / 3 }
                    else -> slideOutHorizontally(tween(360)) { it }
                }
                ContentTransform(enter, exit, targetContentZIndex = if (forward) 1f else -1f, sizeTransform = null)
            },
            label = "Profile navigation",
        ) { screen ->
            when (screen) {
                ProfileDestination.Overview, ProfileDestination.Achievements -> UserProfileScreen(
                    state.copy(avatarBusy = state.avatarBusy || importing), achievementState,
                    onEdit = { profile.accept(ProfileIntent.BeginEdit); navigate(ProfileDestination.Edit) },
                    onSettings = { navigate(ProfileDestination.Settings) }, onPhoto = { sourceSheet = true },
                    onReload = { profile.accept(ProfileIntent.Reload) }, onAchievementIntent = achievements::accept,
                    onShowAll = { navigate(ProfileDestination.Achievements) },
                    achievementsExpanded = destination == ProfileDestination.Achievements,
                    onCollapseAchievements = { destination = ProfileDestination.Overview },
                )
                ProfileDestination.Edit -> EditProfileScreen(state, profile::accept) { destination = ProfileDestination.Overview }
                ProfileDestination.Settings -> SettingsScreen(settingsState, settings::accept, signingOut, signOutFailed, onSignOut) { destination = ProfileDestination.Overview }
                ProfileDestination.Avatar -> {
                    val uri = draft
                    if (uri != null) AvatarEditorScreen(uri, state.avatarBusy, state.failure, { profile.accept(ProfileIntent.UploadAvatar(it)) }, ::closeEditor)
                    else LaunchedEffect(Unit) {
                        if (destination == ProfileDestination.Avatar) destination = ProfileDestination.Overview
                    }
                }
            }
        }
        SyncIndicator(
            visible = ((state.loading && state.profile != null) || achievementState.refreshing) && snackbar.currentSnackbarData == null,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
        )
        }
    }
    achievementState.selected?.let { item -> AchievementDialog(item) { achievements.accept(AchievementsIntent.DismissAchievement) } }
    if (sourceSheet) PhotoSourceSheet(
        hasAvatar = state.profile?.avatarUrl != null,
        onDismiss = { sourceSheet = false },
        onCamera = {
            sourceSheet = false
            try {
                val folder = File(context.cacheDir, "avatar_capture").apply { mkdirs() }
                folder.listFiles()?.filter { System.currentTimeMillis() - it.lastModified() > 86_400_000 }?.forEach { it.delete() }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", File(folder, UUID.randomUUID().toString() + ".jpg"))
                cameraUri = uri.toString()
                camera.launch(uri)
            } catch (_: ActivityNotFoundException) {
                scope.launch { snackbar.showSnackbar(strings.getString(R.string.profile_camera_unavailable)) }
            } catch (_: Exception) {
                scope.launch { snackbar.showSnackbar(strings.getString(R.string.profile_camera_error)) }
            }
        },
        onGallery = {
            sourceSheet = false
            try { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            catch (_: ActivityNotFoundException) { scope.launch { snackbar.showSnackbar(strings.getString(R.string.profile_gallery_unavailable)) } }
        },
        onRemove = { sourceSheet = false; removeDialog = true },
    )
    if (removeDialog) AlertDialog(
        onDismissRequest = { removeDialog = false }, containerColor = backgroundSecondary,
        title = { Text(strings.getString(R.string.profile_photo_remove_title), color = titleMain) },
        text = { Text(strings.getString(R.string.profile_photo_remove_body), color = username) },
        confirmButton = { TextButton(onClick = { removeDialog = false; profile.accept(ProfileIntent.RemoveAvatar) }) { Text(strings.getString(R.string.profile_remove), color = Color(0xFFB4485D)) } },
        dismissButton = { TextButton(onClick = { removeDialog = false }) { Text(strings.getString(R.string.profile_cancel), color = bodySecondary) } },
    )
}
