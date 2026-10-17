package ru.mareanexx.user_profile_impl.presentation.screen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_impl.R
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun ProfileOverviewHeader(
    editEnabled: Boolean,
    settingsEnabled: Boolean,
    onEdit: () -> Unit,
    onSettings: () -> Unit,
) {
    val strings = LocalResources.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(strings.getString(R.string.profile_title), Modifier.weight(1f), style = MaterialTheme.typography.headlineLarge, color = titleMain)
        FilledIconButton(onEdit, Modifier.size(48.dp), enabled = editEnabled, shape = RoundedCornerShape(16.dp),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = bodySecondary, contentColor = Color.White)) {
            Icon(painterResource(CommonR.drawable.edit_icon), strings.getString(R.string.profile_edit_description), Modifier.size(24.dp))
        }
        FilledTonalIconButton(onSettings, Modifier.size(48.dp), enabled = settingsEnabled, shape = RoundedCornerShape(16.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = backgroundSecondary, contentColor = titleMain)) {
            Icon(painterResource(CommonR.drawable.settings_icon), strings.getString(R.string.profile_settings_description), Modifier.size(24.dp))
        }
    }
}
