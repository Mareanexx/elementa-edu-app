package ru.mareanexx.book_impl.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import ru.mareanexx.ui.components.skeleton.BoxSkeleton
import ru.mareanexx.ui.theme.*

@Composable
internal fun BookSkeleton() {
    Column(Modifier.fillMaxWidth().padding(16.dp).testTag("book_skeleton").then(if (LocalAppUiPreferences.current.reduceMotion) Modifier else Modifier.shimmer()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(shape = RoundedCornerShape(24.dp), color = backgroundSecondary) {
            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BoxSkeleton(width = 124, height = 184)
                Column(Modifier.weight(1f).padding(top = 8.dp)) {
                    BoxSkeleton(modifier = Modifier.fillMaxWidth().height(48.dp))
                    Spacer(Modifier.height(12.dp))
                    BoxSkeleton(modifier = Modifier.fillMaxWidth(.8f).height(32.dp))
                    Spacer(Modifier.height(16.dp))
                    BoxSkeleton(modifier = Modifier.fillMaxWidth().height(24.dp))
                }
            }
        }
        BoxSkeleton(modifier = Modifier.fillMaxWidth().height(66.dp))
        BoxSkeleton(modifier = Modifier.fillMaxWidth().height(52.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { repeat(2) { BoxSkeleton(modifier = Modifier.weight(1f).height(52.dp)) } }
        BoxSkeleton(width = 140, height = 22)
        repeat(4) { BoxSkeleton(modifier = Modifier.fillMaxWidth(if (it == 3) .7f else 1f).height(14.dp)) }
    }
}

@Composable
internal fun ReviewSkeleton() {
    Column(Modifier.fillMaxWidth().then(if (LocalAppUiPreferences.current.reduceMotion) Modifier else Modifier.shimmer()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) { BoxSkeleton(modifier = Modifier.fillMaxWidth().height(168.dp)) }
    }
}
