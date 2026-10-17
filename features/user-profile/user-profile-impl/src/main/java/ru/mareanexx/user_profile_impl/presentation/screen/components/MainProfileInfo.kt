package ru.mareanexx.user_profile_impl.presentation.screen.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_api.domain.model.UserProfile
import ru.mareanexx.user_profile_impl.R
import ru.mareanexx.common_ui.R as CommonR

@Composable
fun MainProfileInfo(userData: UserProfile, avatarBusy: Boolean = false, onPhotoClick: () -> Unit = {}) {
    val strings = androidx.compose.ui.platform.LocalResources.current
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(116.dp)) {
            AsyncImage(
                model = userData.avatarUrl,
                contentDescription = strings.getString(R.string.profile_photo_title),
                placeholder = painterResource(R.drawable.user_avatar_placeholder),
                error = painterResource(R.drawable.user_avatar_placeholder),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(112.dp).clip(CircleShape).background(backgroundSecondary),
            )
            FilledIconButton(
                onClick = onPhotoClick,
                enabled = !avatarBusy,
                modifier = Modifier.size(48.dp).align(Alignment.BottomEnd).border(3.dp, backgroundMain, CircleShape),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = bodySecondary, contentColor = ColorWhite),
            ) {
                if (avatarBusy) CircularProgressIndicator(Modifier.size(20.dp), color = ColorWhite, strokeWidth = 2.dp)
                else Icon(painterResource(CommonR.drawable.photo_camera_icon), strings.getString(R.string.profile_photo_change), Modifier.size(23.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(userData.fullName, style = MaterialTheme.typography.displayMedium, color = titleMain, textAlign = TextAlign.Center)
        Text(strings.getString(R.string.profile_username_value, userData.username), style = MaterialTheme.typography.labelLarge, color = username, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth().background(descriptionBackground, RoundedCornerShape(16.dp)).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(CommonR.drawable.notes_icon), null, tint = notesIcon, modifier = Modifier.size(24.dp))
            Text(userData.description.ifBlank { strings.getString(R.string.profile_bio_empty) }, style = MaterialTheme.typography.labelMedium, color = titleMain, modifier = Modifier.weight(1f))
        }
    }
}

private val ColorWhite = androidx.compose.ui.graphics.Color.White
