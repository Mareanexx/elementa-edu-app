package ru.mareanexx.user_profile_impl.presentation.photo

import ru.mareanexx.user_profile_impl.R
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.mareanexx.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoSourceSheet(hasAvatar: Boolean, onDismiss: () -> Unit, onCamera: () -> Unit, onGallery: () -> Unit, onRemove: () -> Unit) {
    val strings = androidx.compose.ui.platform.LocalResources.current
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = backgroundSecondary, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = {
            Box(Modifier.fillMaxWidth().height(24.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(32.dp, 4.dp).background(username.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
            }
        }) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
            Text(strings.getString(R.string.profile_photo_title), style = MaterialTheme.typography.displayMedium, color = titleMain)
            Text(strings.getString(R.string.profile_photo_source_subtitle), style = MaterialTheme.typography.labelLarge, color = username, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
            FilledTonalButton(onCamera, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = descriptionBackground, contentColor = titleMain)) {
                Text(strings.getString(R.string.profile_photo_camera), style = MaterialTheme.typography.displaySmall)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onGallery, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text(strings.getString(R.string.profile_photo_gallery), color = bodySecondary, style = MaterialTheme.typography.displaySmall)
            }
            if (hasAvatar) TextButton(onRemove, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text(strings.getString(R.string.profile_photo_remove), color = Color(0xFFB4485D), style = MaterialTheme.typography.displaySmall)
            }
        }
    }
}
