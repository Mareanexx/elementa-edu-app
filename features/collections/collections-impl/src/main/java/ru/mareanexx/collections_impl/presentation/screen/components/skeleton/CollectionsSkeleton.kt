package ru.mareanexx.collections_impl.presentation.screen.components.skeleton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import ru.mareanexx.ui.theme.LocalAppUiPreferences
import ru.mareanexx.ui.theme.skeletonBackground
import ru.mareanexx.collections_impl.presentation.screen.components.collectionCardHeight

@Composable
internal fun CollectionsSkeleton(overview: Boolean = false, books: Boolean = false) {
    val motion = !LocalAppUiPreferences.current.reduceMotion
    Column(Modifier.fillMaxWidth().padding(16.dp).then(if (motion) Modifier.shimmer() else Modifier), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (overview) {
            SkeletonBlock(Modifier.fillMaxWidth().height(188.dp), radius = 26)
            SkeletonBlock(Modifier.width(180.dp).height(24.dp))
        }
        repeat(3) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBlock(Modifier.weight(1f).height(if (books) 144.dp else collectionCardHeight()), radius = if (books) 16 else 22)
                if (!books) SkeletonBlock(Modifier.weight(1f).height(collectionCardHeight()), radius = 22)
            }
        }
    }
}

@Composable
private fun SkeletonBlock(modifier: Modifier, radius: Int = 16) {
    Box(modifier.clip(RoundedCornerShape(radius.dp)).background(skeletonBackground))
}
