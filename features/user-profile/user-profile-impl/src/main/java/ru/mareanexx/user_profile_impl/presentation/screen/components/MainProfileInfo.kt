package ru.mareanexx.user_profile_impl.presentation.screen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.mareanexx.ui.theme.backgroundMain
import ru.mareanexx.ui.theme.bodySecondary
import ru.mareanexx.ui.theme.descriptionBackground
import ru.mareanexx.ui.theme.notesIcon
import ru.mareanexx.ui.theme.onBackgroundMain
import ru.mareanexx.ui.theme.outerShadow
import ru.mareanexx.ui.theme.titleMain
import ru.mareanexx.ui.theme.username
import ru.mareanexx.ui.utils.GlobalUiConsts.HORIZONTAL_PADDING
import ru.mareanexx.user_profile_impl.R
import ru.mareanexx.user_profile_impl.presentation.model.ProfileMainData
import ru.mareanexx.common_ui.R as CommonUiR

@Composable
fun MainProfileInfo(
    userData: ProfileMainData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HORIZONTAL_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            // AsyncImage( TODO: заменить на AsyncImage
            //     model = "",
            //     contentDescription = null,
            //     modifier = Modifier.size(128.dp),
            // )
            Image(
                painter = painterResource(R.drawable.user_avatar_placeholder),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = outerShadow, spotColor = outerShadow,
                    )
                    .size(128.dp),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .border(width = 2.dp, color = backgroundMain, shape = RoundedCornerShape(100.dp))
                    .background(color = bodySecondary, shape = RoundedCornerShape(100.dp))
                    .padding(8.dp),
            ) {
                Icon(
                    painter = painterResource(CommonUiR.drawable.photo_camera_icon),
                    tint = onBackgroundMain,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = userData.fullName,
            style = MaterialTheme.typography.displayMedium,
            color = titleMain,
        )

        Text(
            text = userData.username,
            style = MaterialTheme.typography.labelLarge,
            color = username,
        )

        Spacer(modifier = Modifier.height(20.dp))

        ProfileDescription(description = userData.description)
    }
}

@Composable
private fun ProfileDescription(
    description: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = descriptionBackground, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            painter = painterResource(CommonUiR.drawable.notes_icon),
            tint = notesIcon,
            contentDescription = null,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.labelMedium,
            color = titleMain,
            fontWeight = FontWeight.Medium,
        )
    }
}
