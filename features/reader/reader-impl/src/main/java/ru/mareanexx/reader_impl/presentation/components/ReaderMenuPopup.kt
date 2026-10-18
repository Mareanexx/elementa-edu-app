package ru.mareanexx.reader_impl.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import ru.mareanexx.reader_api.domain.model.ReaderTheme
import ru.mareanexx.reader_impl.R
import ru.mareanexx.reader_impl.presentation.model.ReaderIntent
import ru.mareanexx.reader_impl.presentation.model.ReaderPanel
import ru.mareanexx.common_ui.R as CommonR

@Composable
fun ReaderMenuPopup(
    visible: Boolean,
    theme: ReaderTheme,
    accept: (ReaderIntent) -> Unit,
) {
    val transition = updateTransition(visible, label = "reader_menu")
    if (!transition.currentState && !transition.targetState) return
    val palette = readerPalette(theme)
    val density = LocalDensity.current
    val windowWidth = LocalWindowInfo.current.containerSize.width
    val width = with(density) { (windowWidth.toDp() - 32.dp).coerceAtMost(320.dp) }
    Popup(
        alignment = Alignment.BottomEnd,
        offset = with(density) { IntOffset(-16.dp.roundToPx(), -79.dp.roundToPx()) },
        onDismissRequest = { if (visible) accept(ReaderIntent.Panel(ReaderPanel.None)) },
        properties = PopupProperties(focusable = visible, dismissOnBackPress = true),
    ) {
        Column(Modifier.width(width).testTag("reader_navigation_menu"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ReaderMenuBubble(transition, 2, R.string.reader_contents, R.drawable.reader_list, ReaderPanel.Chapters, palette, accept)
            ReaderMenuBubble(transition, 1, R.string.reader_notes, CommonR.drawable.stylus_note_icon, ReaderPanel.Notes, palette, accept)
            ReaderMenuBubble(transition, 0, R.string.reader_settings, CommonR.drawable.settings_icon, ReaderPanel.Settings, palette, accept)
        }
    }
}

@Composable
private fun ReaderMenuBubble(
    transition: Transition<Boolean>,
    order: Int,
    label: Int,
    icon: Int,
    destination: ReaderPanel,
    palette: ReaderPalette,
    accept: (ReaderIntent) -> Unit,
) {
    val progress by transition.animateFloat(
        transitionSpec = {
            if (targetState) tween(durationMillis = 210, delayMillis = order * 45, easing = FastOutSlowInEasing)
            else tween(durationMillis = 120, easing = FastOutLinearInEasing)
        },
        label = "reader_menu_${destination.name}",
    ) { if (it) 1f else 0f }
    Surface(
        onClick = { accept(ReaderIntent.Panel(destination)) },
        enabled = transition.targetState,
        modifier = Modifier.fillMaxWidth().graphicsLayer {
            alpha = progress
            translationY = (1f - progress) * 14.dp.toPx()
            scaleX = .94f + progress * .06f
            scaleY = .94f + progress * .06f
            transformOrigin = TransformOrigin(1f, 1f)
        }.testTag("reader_menu_${destination.name}"),
        shape = RoundedCornerShape(28.dp),
        color = palette.surface,
        contentColor = palette.ink,
        border = BorderStroke(1.dp, palette.ink.copy(alpha = .05f)),
        shadowElevation = 2.dp,
    ) {
        Row(
            Modifier.heightIn(min = 54.dp).padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(label), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, fontSize = 16.sp)
            Icon(painterResource(icon), null, Modifier.size(24.dp), tint = palette.accent)
        }
    }
}
