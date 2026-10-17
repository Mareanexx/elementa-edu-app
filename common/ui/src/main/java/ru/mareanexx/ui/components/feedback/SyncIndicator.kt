package ru.mareanexx.ui.components.feedback

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ru.mareanexx.common_ui.R
import ru.mareanexx.ui.theme.*

/** Плавающий индикатор располагается поверх содержимого и не меняет высоту экрана. */
@Composable
fun SyncIndicator(visible: Boolean, modifier: Modifier = Modifier) {
    val reduceMotion = LocalAppUiPreferences.current.reduceMotion
    AnimatedVisibility(visible, modifier, enter = if (reduceMotion) EnterTransition.None else fadeIn(), exit = if (reduceMotion) ExitTransition.None else fadeOut()) {
        Surface(shape = RoundedCornerShape(14.dp), color = titleMain, modifier = Modifier.testTag("content_sync_indicator")
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(Modifier.size(14.dp), color = Color.White, strokeWidth = 1.5.dp)
                Text(stringResource(R.string.content_synchronizing), color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
