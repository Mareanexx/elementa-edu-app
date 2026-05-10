package ru.mareanexx.user_profile_impl.presentation.screen.components.skeleton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
fun UserProfileSkeleton() {
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

        // основная инфа профиля
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BoxSkeleton(width = 128, height = 128, shape = CircleShape)
            Spacer(modifier = Modifier.height(20.dp))
            BoxSkeleton(width = 310, height = 26)
            Spacer(modifier = Modifier.height(7.dp))
            BoxSkeleton(width = 108, height = 20)
            Spacer(modifier = Modifier.height(20.dp))
            BoxSkeleton(modifier = Modifier.fillMaxWidth().height(56.dp))
        }

        // статистика
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            repeat(3) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BoxSkeleton(width = 56, height = 56)
                    BoxSkeleton(width = 60, height = 10)
                    BoxSkeleton(width = 100, height = 10)
                    BoxSkeleton(width = 40, height = 30)
                }
            }
        }

        // достижения
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BoxSkeleton(height = 20, width = 220)
                BoxSkeleton(height = 20, width = 85)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                repeat(4) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        BoxSkeleton(height = 86, width = 85)
                        BoxSkeleton(height = 20, width = 85)
                        BoxSkeleton(height = 12, width = 75)
                    }
                }
            }
        }
    }
}
