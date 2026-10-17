package ru.mareanexx.user_profile_impl.presentation.screen.components.skeleton

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import ru.mareanexx.ui.components.skeleton.BoxSkeleton
import ru.mareanexx.ui.theme.LocalAppUiPreferences
import ru.mareanexx.user_profile_impl.R

@Composable
internal fun ProfileShimmer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shimmer = rememberShimmer(ShimmerBounds.View)
    Box(if (LocalAppUiPreferences.current.reduceMotion) modifier else modifier.shimmer(shimmer)) { content() }
}

/** Повторяет геометрию профиля, чтобы загрузка не вызывала скачков содержимого. */
@Composable
fun UserProfileSkeleton() {
    val description = stringResource(R.string.profile_loading)
    ProfileShimmer(Modifier.fillMaxWidth().testTag("profile_skeleton").semantics { contentDescription = description }) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            BoxSkeleton(width = 116, height = 116, shape = CircleShape)
            Spacer(Modifier.height(12.dp))
            BoxSkeleton(modifier = Modifier.fillMaxWidth(0.72f).height(28.dp))
            Spacer(Modifier.height(6.dp))
            BoxSkeleton(width = 112, height = 22)
            Spacer(Modifier.height(16.dp))
            BoxSkeleton(modifier = Modifier.fillMaxWidth().height(64.dp))
            Row(Modifier.fillMaxWidth().padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { BoxSkeleton(modifier = Modifier.weight(1f).height(148.dp)) }
            }
        }
    }
}

@Composable
internal fun AchievementStripSkeleton() {
    ProfileShimmer(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    BoxSkeleton(modifier = Modifier.fillMaxWidth().aspectRatio(1f))
                    Spacer(Modifier.height(8.dp))
                    BoxSkeleton(modifier = Modifier.fillMaxWidth().height(10.dp))
                    Spacer(Modifier.height(4.dp))
                    BoxSkeleton(modifier = Modifier.fillMaxWidth(0.8f).height(10.dp))
                    Spacer(Modifier.height(6.dp))
                    BoxSkeleton(modifier = Modifier.fillMaxWidth(0.7f).height(10.dp))
                }
            }
        }
    }
}

@Composable
internal fun AchievementGridSkeleton() {
    ProfileShimmer(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BoxSkeleton(width = 168, height = 24)
            repeat(2) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(2) { BoxSkeleton(modifier = Modifier.weight(1f).height(196.dp)) }
                }
            }
        }
    }
}
