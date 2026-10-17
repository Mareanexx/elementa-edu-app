package ru.mareanexx.user_profile_impl.presentation.components

import ru.mareanexx.user_profile_impl.R
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_api.domain.model.ProfileError
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun ProfileTopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    val strings = androidx.compose.ui.platform.LocalResources.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.offset(x = (-8).dp)) {
                Icon(painterResource(CommonR.drawable.arrow_back_icon), strings.getString(R.string.profile_back), tint = titleMain)
            }
        }
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.headlineLarge, color = titleMain)
        actions()
    }
}

@Composable
internal fun ProfilePrimaryButton(text: String, loading: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick, enabled = enabled && !loading, modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bodySecondary), shape = RoundedCornerShape(30.dp)) {
        if (loading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
        else Text(text, style = MaterialTheme.typography.titleSmall, color = Color.White)
    }
}

@Composable
internal fun ProfileErrorBanner(error: ProfileError, retry: (() -> Unit)? = null) {
    val strings = androidx.compose.ui.platform.LocalResources.current
    Surface(color = Color(0xFFF8E4E8), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
            Text(profileErrorText(error), color = Color(0xFF9F374C), style = MaterialTheme.typography.labelMedium)
            if (retry != null) TextButton(onClick = retry, contentPadding = PaddingValues(0.dp)) { Text(strings.getString(R.string.profile_retry), color = bodySecondary) }
        }
    }
}

@Composable
internal fun ProfileLoading(label: String) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(color = bodySecondary)
        Spacer(Modifier.height(16.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = username)
    }
}

@androidx.annotation.StringRes
internal fun profileErrorResource(error: ProfileError): Int = when (error) {
    ProfileError.InvalidField -> R.string.profile_error_fields
    ProfileError.Required -> R.string.profile_error_required
    ProfileError.InvalidName -> R.string.profile_error_name
    ProfileError.InvalidUsername -> R.string.profile_error_username
    ProfileError.DescriptionTooLong -> R.string.profile_error_bio_length
    ProfileError.UsernameTaken -> R.string.profile_error_username_taken
    ProfileError.Offline -> R.string.profile_error_offline
    ProfileError.Timeout -> R.string.profile_error_timeout
    ProfileError.Unauthorized -> R.string.profile_error_session
    ProfileError.Unavailable -> R.string.profile_error_unavailable
    ProfileError.InvalidResponse -> R.string.profile_error_response
    ProfileError.Storage -> R.string.profile_error_storage
    ProfileError.InvalidImage -> R.string.profile_error_image
    ProfileError.TooLarge -> R.string.profile_error_image_size
    ProfileError.RateLimited -> R.string.profile_error_rate_limit
}


@Composable
internal fun profileErrorText(error: ProfileError): String {
    val strings = androidx.compose.ui.platform.LocalResources.current
    return strings.getString(profileErrorResource(error))
}
