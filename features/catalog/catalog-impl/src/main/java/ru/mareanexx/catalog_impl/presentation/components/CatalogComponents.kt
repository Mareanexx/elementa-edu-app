package ru.mareanexx.catalog_impl.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.catalog_api.domain.model.CatalogError
import ru.mareanexx.catalog_impl.R
import ru.mareanexx.ui.theme.*
import ru.mareanexx.ui.components.skeleton.BoxSkeleton
import com.valentinilk.shimmer.shimmer
import ru.mareanexx.common_ui.R as CommonR

internal fun Modifier.catalogClick(enabled: Boolean = true, onClick: () -> Unit) = clickable(enabled = enabled, interactionSource = null, indication = null, onClick = onClick)

@Composable
internal fun CatalogTopBar(title: String, onBack: () -> Unit, search: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onBack) { Icon(painterResource(CommonR.drawable.arrow_back_icon), stringResource(R.string.catalog_back), tint = titleMain) }
        Text(title, Modifier.weight(1f).padding(horizontal = 8.dp), style = SecondaryScreenTitle, color = titleMain)
        if (search != null) IconButton(search) { Icon(painterResource(R.drawable.catalog_search), stringResource(R.string.catalog_search), tint = titleMain) }
    }
}

@Composable
internal fun CatalogErrorBanner(error: CatalogError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier, color = Color(0xFFF7E8EC), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(errorText(error), Modifier.weight(1f), color = Color(0xFF9D4F62), style = MaterialTheme.typography.labelMedium)
            TextButton(onRetry) { Text(stringResource(if (error == CatalogError.Conflict || error == CatalogError.NotFound) R.string.catalog_refresh else R.string.catalog_retry), color = bodySecondary) }
        }
    }
}

@Composable
internal fun CatalogEmpty(title: String, body: String, modifier: Modifier = Modifier, icon: Int = CommonR.drawable.menu_book_icon) {
    Column(modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(color = tagVioletBackground, shape = RoundedCornerShape(22.dp)) { Icon(painterResource(icon), null, Modifier.padding(18.dp).size(32.dp), tint = bodySecondary) }
        Text(title, color = titleMain, style = MaterialTheme.typography.titleSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(body, color = username, style = MaterialTheme.typography.labelMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
internal fun CatalogSkeleton(modifier: Modifier = Modifier) {
    val reduce = LocalAppUiPreferences.current.reduceMotion
    Column(modifier.fillMaxWidth().testTag("catalog_skeleton").then(if (reduce) Modifier else Modifier.shimmer()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        BoxSkeleton(modifier = Modifier.fillMaxWidth().height(202.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { repeat(2) { BoxSkeleton(modifier = Modifier.weight(1f).height(74.dp)) } }
        BoxSkeleton(modifier = Modifier.width(130.dp).height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { repeat(4) { BoxSkeleton(modifier = Modifier.weight(1f).height(90.dp)) } }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { repeat(2) { BoxSkeleton(modifier = Modifier.weight(1f).height(240.dp)) } }
    }
}

@Composable
internal fun CatalogTabs(labels: List<String>, selectedIndex: Int, enabled: Boolean = true, onSelect: (Int) -> Unit) {
    val list = androidx.compose.foundation.lazy.rememberLazyListState()
    val reduce = LocalAppUiPreferences.current.reduceMotion
    LaunchedEffect(selectedIndex) {
        if (reduce) list.scrollToItem((selectedIndex - 1).coerceAtLeast(0))
        else list.animateScrollToItem((selectedIndex - 1).coerceAtLeast(0))
    }
    LazyRow(state = list, contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        itemsIndexed(labels) { index, label ->
            val selected = index == selectedIndex
            val color by androidx.compose.animation.animateColorAsState(if (selected) bodySecondary else authFieldBackground, label = "catalog_tab_color")
            Box(Modifier.heightIn(min = 48.dp).testTag("catalog_tab_$index").selectable(selected, enabled = enabled, role = Role.Tab, interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(index) }, contentAlignment = Alignment.Center) {
                Surface(color = color, shape = RoundedCornerShape(50)) { Text(label, Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = if (selected) Color.White else username, style = MaterialTheme.typography.labelMedium) }
            }
        }
    }
}
