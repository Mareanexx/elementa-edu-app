package ru.mareanexx.user_profile_impl.presentation.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_impl.R
import ru.mareanexx.user_profile_impl.presentation.profile.ProfileUiState
import ru.mareanexx.user_profile_impl.presentation.achievements.*
import ru.mareanexx.user_profile_impl.presentation.components.*
import ru.mareanexx.user_profile_impl.presentation.screen.components.*
import ru.mareanexx.user_profile_impl.presentation.screen.components.skeleton.UserProfileSkeleton

@Composable
fun UserProfileScreen(
    state: ProfileUiState,
    achievements: AchievementsUiState,
    onEdit: () -> Unit,
    onSettings: () -> Unit,
    onPhoto: () -> Unit,
    onReload: () -> Unit,
    onAchievementIntent: (AchievementsIntent) -> Unit,
    onShowAll: () -> Unit,
    achievementsExpanded: Boolean = false,
    onCollapseAchievements: () -> Unit = {},
) {
    val strings = LocalResources.current
    val density = LocalDensity.current
    val reduceMotion = LocalAppUiPreferences.current.reduceMotion
    val openAll by rememberUpdatedState(onShowAll)
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (achievementsExpanded) 1f else if (dragging) dragProgress else 0f,
        animationSpec = if (dragging || reduceMotion) snap() else tween(460, easing = FastOutSlowInEasing),
        label = "Achievements expansion",
    )
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val peek = 240.dp * density.fontScale.coerceIn(1f, 1.3f)
        val sheetHeight = peek + (maxHeight - peek) * progress
        val travel = with(density) { (maxHeight - peek).coerceAtLeast(1.dp).toPx() }
        Scaffold(
            modifier = if (progress > 0f) Modifier.clearAndSetSemantics {} else Modifier,
            containerColor = backgroundMain,
            topBar = {
                ProfileOverviewHeader(state.profile != null && !state.avatarBusy, !state.avatarBusy, onEdit, onSettings)
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(bottom = peek + 16.dp)) {
                when {
                    state.loading && state.profile == null -> UserProfileSkeleton()
                    state.profile == null -> Box(Modifier.padding(16.dp)) { state.failure?.let { ProfileErrorBanner(it.error, onReload) } }
                    else -> {
                        Spacer(Modifier.height(8.dp))
                        MainProfileInfo(state.profile, state.avatarBusy, onPhoto)
                        Spacer(Modifier.height(20.dp))
                        ProfileStatistics(state.profile.statistics)
                        if (state.failure != null) Box(Modifier.padding(16.dp)) { ProfileErrorBanner(state.failure.error, onReload) }
                    }
                }
            }
        }
        Surface(
            color = backgroundSecondary,
            shape = RoundedCornerShape(topStart = 30.dp * (1f - progress), topEnd = 30.dp * (1f - progress)),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(sheetHeight)
                .testTag("achievements_sheet")
                .pointerInput(achievementsExpanded, travel) {
                    if (!achievementsExpanded) {
                        var pulled = 0f
                        detectVerticalDragGestures(
                            onDragStart = { pulled = 0f; dragging = true },
                            onVerticalDrag = { change, amount ->
                                change.consume()
                                pulled = (pulled - amount).coerceIn(0f, travel)
                                dragProgress = pulled / travel
                            },
                            onDragEnd = {
                                // Жест и кнопка управляют одной панелью: содержимое не заменяется навигационным экраном.
                                if (pulled >= with(density) { 56.dp.toPx() }) openAll()
                                dragging = false
                            },
                            onDragCancel = { dragging = false },
                        )
                    }
                },
        ) {
            Box(Modifier.fillMaxSize()) {
                if (progress < 0.15f) Column(Modifier.graphicsLayer { alpha = (1f - progress / 0.15f).coerceIn(0f, 1f) }) {
                    Box(Modifier.fillMaxWidth().height(20.dp).clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClickLabel = strings.getString(R.string.profile_sheet_expand),
                        onClick = onShowAll,
                    ), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(width = 32.dp, height = 4.dp).background(username.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                    }
                    Achievements(achievements, onAchievementIntent, onShowAll)
                }
                if (progress > 0.15f) Box(Modifier.fillMaxSize().graphicsLayer { alpha = ((progress - 0.15f) / 0.5f).coerceIn(0f, 1f) }) {
                    AchievementsScreen(achievements, onAchievementIntent, onCollapseAchievements)
                }
            }
        }
    }
}
