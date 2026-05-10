package ru.mareanexx.collections_impl.presentation.screen.components.skeleton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9_PRO
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import ru.mareanexx.ui.components.skeleton.BoxSkeleton

@Preview(device = PIXEL_9_PRO)
@Composable
fun CollectionsSkeleton() {
    val shimmer = rememberShimmer(ShimmerBounds.View)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .shimmer(shimmer),
    ) {
        // тулбар
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BoxSkeleton(height = 28, width = 220)
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                repeat(2) { BoxSkeleton(height = 30, width = 30) }
            }
        }

        Spacer(modifier = Modifier.padding(vertical = 16.dp))

        BoxSkeleton(modifier = Modifier.fillMaxWidth().height(200.dp))

        Spacer(modifier = Modifier.padding(vertical = 16.dp))

        // хедер блока контента
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BoxSkeleton(height = 20, width = 130)
            BoxSkeleton(height = 20, width = 85)
        }

        // грид из карточек коллекций
        repeat(4) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                repeat(2) {
                    BoxSkeleton(modifier = Modifier.weight(0.5f).height(145.dp))
                }
            }
        }
    }
}
