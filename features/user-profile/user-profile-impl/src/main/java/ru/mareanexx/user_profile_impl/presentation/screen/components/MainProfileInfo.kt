package ru.mareanexx.user_profile_impl.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_api.domain.model.UserProfile
import ru.mareanexx.user_profile_impl.R
import ru.mareanexx.common_ui.R as CommonR

@Composable
fun MainProfileInfo(userData: UserProfile, avatarBusy: Boolean = false, onPhotoClick: () -> Unit = {}) {
    val strings = LocalResources.current
    val initials = remember(userData.fullName) {
        userData.fullName.trim().split(Regex("\\s+")).mapNotNull { word -> word.firstOrNull(Char::isLetter)?.uppercaseChar() }.take(2).joinToString("")
    }
    Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(26.dp), color = backgroundSecondary) {
        Column {
            Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF6254AE), Color(0xFF494181))))) {
                ProfileIdentityArtwork(Modifier.matchParentSize())
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(112.dp)) {
                        SubcomposeAsyncImage(
                            model = userData.avatarUrl,
                            contentDescription = strings.getString(R.string.profile_photo_title),
                            loading = { ProfileAvatarInitials(initials) },
                            error = { ProfileAvatarInitials(initials) },
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(104.dp).align(Alignment.Center).clip(CircleShape)
                                .background(Color(0xFFECE5F8)).border(3.dp, Color.White.copy(alpha = .8f), CircleShape),
                        )
                        FilledIconButton(
                            onClick = onPhotoClick,
                            enabled = !avatarBusy,
                            modifier = Modifier.size(48.dp).align(Alignment.BottomEnd).border(3.dp, Color(0xFF5D50A3), CircleShape),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFF6F2FF), contentColor = bodySecondary),
                        ) {
                            if (avatarBusy) CircularProgressIndicator(Modifier.size(20.dp), color = bodySecondary, strokeWidth = 2.dp)
                            else Icon(painterResource(CommonR.drawable.photo_camera_icon), strings.getString(R.string.profile_photo_change), Modifier.size(22.dp))
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(userData.fullName, style = MaterialTheme.typography.titleMedium, color = Color.White,
                            lineHeight = 25.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Text(strings.getString(R.string.profile_username_value, userData.username), Modifier.padding(top = 6.dp),
                            style = MaterialTheme.typography.labelMedium, color = Color(0xFFE7DFF8), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().heightIn(min = 76.dp).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).background(Color(0xFFEEEAF7), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(painterResource(CommonR.drawable.notes_icon), null, tint = Color(0xFF8875AA), modifier = Modifier.size(20.dp))
                }
                Text(userData.description.ifBlank { strings.getString(R.string.profile_bio_empty) }, style = MaterialTheme.typography.labelMedium,
                    color = if (userData.description.isBlank()) username else titleMain, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ProfileAvatarInitials(initials: String) {
    Box(Modifier.fillMaxSize().background(Color(0xFFECE5F8)), contentAlignment = Alignment.Center) {
        Text(initials, style = MaterialTheme.typography.displayMedium, fontSize = 32.sp, color = Color(0xFF6D5D99))
    }
}
