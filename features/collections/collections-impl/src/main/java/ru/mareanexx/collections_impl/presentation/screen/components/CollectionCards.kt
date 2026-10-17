package ru.mareanexx.collections_impl.presentation.screen.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.collections_api.domain.model.BookCollection
import ru.mareanexx.collections_api.domain.model.CollectionCover
import ru.mareanexx.collections_impl.R
import ru.mareanexx.collections_impl.presentation.CollectionsIntent
import ru.mareanexx.collections_impl.presentation.components.*
import ru.mareanexx.ui.theme.*
import ru.mareanexx.common_ui.R as CommonR

@Composable
internal fun FavoriteCollectionCard(item: BookCollection, onClick: () -> Unit) {
    val strings = LocalResources.current
    val extraHeight = (maxOf(0f, LocalDensity.current.fontScale - 1f) * 100).dp
    BoxWithConstraints(Modifier.fillMaxWidth().height(188.dp + extraHeight).clip(RoundedCornerShape(26.dp))
        .background(Brush.linearGradient(listOf(Color(0xFF6254AE), Color(0xFF494181))))
        .collectionPress(onClick)) {
        CollectionShelfArtwork(CollectionCover.Notes, Modifier.align(Alignment.CenterEnd).width(maxWidth * .52f).fillMaxHeight(), featured = true)
        Column(Modifier.width(maxWidth * .61f).fillMaxHeight().padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(painterResource(CommonR.drawable.star_filled_icon), null, Modifier.size(14.dp), tint = favoriteStar)
                    Text(strings.getString(R.string.collections_favorite_eyebrow), color = Color(0xFFE6DFF9), style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                }
                Text(collectionTitle(item), Modifier.padding(top = 10.dp), color = Color.White,
                    style = MaterialTheme.typography.titleMedium, fontSize = 26.sp, lineHeight = 29.sp)
                Text(bookCount(item.bookCount), Modifier.padding(top = 4.dp), color = Color(0xFFE6DFF9), style = MaterialTheme.typography.labelMedium)
            }
            FilledTonalButton(onClick, modifier = Modifier.heightIn(min = 38.dp), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFF5F1FF), contentColor = Color(0xFF514687))) {
                Text(strings.getString(R.string.collections_open), style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(12.dp))
                Icon(painterResource(CommonR.drawable.arrow_right_alt_icon), null, Modifier.size(20.dp))
            }
        }
    }
}

@Composable
internal fun CollectionCard(item: BookCollection, onIntent: (CollectionsIntent) -> Unit) {
    val strings = LocalResources.current
    var menu by remember(item.id) { mutableStateOf(false) }
    Surface(shape = RoundedCornerShape(22.dp), color = backgroundSecondary,
        modifier = Modifier.fillMaxWidth().height(collectionCardHeight()).testTag("collection_${item.id}")
            .collectionPress { onIntent(CollectionsIntent.OpenCollection(item.id)) }) {
        Column {
            Box(Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)) {
                CollectionCoverImage(item.cover, item.image, Modifier.fillMaxWidth().height(112.dp).clip(RoundedCornerShape(16.dp)))
                if (item.isFavorite) Surface(Modifier.padding(8.dp), shape = RoundedCornerShape(10.dp), color = Color.White.copy(alpha = .92f)) {
                    Icon(painterResource(CommonR.drawable.star_filled_icon), null, Modifier.padding(6.dp).size(20.dp), tint = Color(0xFFB88936))
                } else Box(Modifier.align(Alignment.TopEnd)) {
                    IconButton({ menu = true }, Modifier.size(44.dp)) {
                        Surface(shape = RoundedCornerShape(10.dp), color = backgroundSecondary.copy(alpha = .94f)) {
                            Icon(painterResource(CommonR.drawable.more_horiz_icon), strings.getString(R.string.collections_more, collectionTitle(item)),
                                Modifier.padding(5.dp).size(20.dp), tint = titleMain)
                        }
                    }
                    DropdownMenu(menu, { menu = false }, shape = RoundedCornerShape(16.dp), containerColor = backgroundSecondary) {
                        DropdownMenuItem(text = { Text(strings.getString(R.string.collections_edit)) }, onClick = { menu = false; onIntent(CollectionsIntent.Edit(item.id)) })
                        DropdownMenuItem(text = { Text(strings.getString(R.string.collections_delete), color = Color(0xFFB4485D)) }, onClick = { menu = false; onIntent(CollectionsIntent.Delete(item.id)) })
                    }
                }
            }
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(collectionTitle(item), style = MaterialTheme.typography.displaySmall, fontSize = 14.sp, lineHeight = 17.sp,
                    color = titleMain, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(bookCount(item.bookCount), Modifier.padding(top = 3.dp), style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp, lineHeight = 14.sp, maxLines = 1, color = username)
            }
        }
    }
}

@Composable
internal fun CreateCollectionCard(onClick: () -> Unit) {
    val strings = LocalResources.current
    val shape = RoundedCornerShape(22.dp)
    Column(Modifier.fillMaxWidth().height(collectionCardHeight()).clip(shape)
        .background(Color.White.copy(alpha = .3f)).border(1.dp, bodySecondary.copy(alpha = .16f), shape)
        .collectionPress(onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(shape = RoundedCornerShape(16.dp), color = bodySecondary.copy(alpha = .09f)) {
            Icon(painterResource(CommonR.drawable.add_icon), null, Modifier.padding(12.dp).size(24.dp), tint = bodySecondary)
        }
        Text(strings.getString(R.string.collections_new_card), Modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp),
            style = MaterialTheme.typography.labelMedium, color = bodySecondary)
        Text(strings.getString(R.string.collections_new_card_hint), Modifier.padding(top = 3.dp),
            style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, color = username)
    }
}

@Composable
internal fun collectionCardHeight() = 128.dp + maxOf(64.dp, (52 * LocalDensity.current.fontScale + 12).dp)

/** Небольшое изменение прозрачности даёт отклик без скачка размеров и стандартного ripple. */
@Composable
private fun Modifier.collectionPress(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val alpha by animateFloatAsState(if (pressed) .88f else 1f,
        animationSpec = tween(if (LocalAppUiPreferences.current.reduceMotion) 0 else 120), label = "collection_press")
    return graphicsLayer { this.alpha = alpha }.clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
}
