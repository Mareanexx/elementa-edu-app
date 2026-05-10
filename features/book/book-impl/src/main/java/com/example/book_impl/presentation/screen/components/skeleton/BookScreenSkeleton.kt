package com.example.book_impl.presentation.screen.components.skeleton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9_PRO
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import ru.mareanexx.ui.components.skeleton.BoxSkeleton
import ru.mareanexx.common_ui.R as CommonUiR

@Preview(device = PIXEL_9_PRO)
@Composable
fun BookSkeleton() {
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BoxSkeleton(height = 30, width = 30)
            BoxSkeleton(height = 20, width = 80)
            BoxSkeleton(height = 30, width = 30)
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
            BoxSkeleton(height = 226, width = 156)

            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // теги книги
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    BoxSkeleton(width = 78, height = 22)
                    BoxSkeleton(width = 100, height = 22)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    BoxSkeleton(width = 50, height = 22)
                    BoxSkeleton(width = 58, height = 22)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // название книги
                BoxSkeleton(width = 170, height = 26)
                BoxSkeleton(width = 70, height = 26)

                Spacer(modifier = Modifier.height(8.dp))

                // автор
                BoxSkeleton(width = 140, height = 20)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // характеристики
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            repeat(4) {
                Column(
                    modifier = Modifier.width(75.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BoxSkeleton(modifier = Modifier.fillMaxWidth().height(24.dp))
                    BoxSkeleton(width = 35, height = 10)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // кнопки
        repeat(2) {
            BoxSkeleton(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp),
                shape = RoundedCornerShape(24.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        // описание

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BoxSkeleton(height = 20, width = 130)
            BoxSkeleton(height = 20, width = 85)
        }

        Spacer(modifier = Modifier.height(12.dp))

        repeat(6) {
            BoxSkeleton(modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(16.dp))
        }

        Spacer(modifier = Modifier.height(28.dp))

        // рейтинг и отзывы
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BoxSkeleton(height = 20, width = 130)
            BoxSkeleton(height = 20, width = 85)
        }

        Row(
            modifier = Modifier.padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                BoxSkeleton(width = 60, height = 50)
                Row(modifier = Modifier.padding(top = 6.dp)) {
                    repeat(5) {
                        Icon(
                            painter = painterResource(CommonUiR.drawable.star_filled_icon),
                            tint = Color.LightGray,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
                BoxSkeleton(width = 130, height = 20)
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(5) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BoxSkeleton(width = 16, height = 16)
                        BoxSkeleton(modifier = Modifier.fillMaxWidth().height(8.dp))
                    }
                }
            }
        }
    }
}
