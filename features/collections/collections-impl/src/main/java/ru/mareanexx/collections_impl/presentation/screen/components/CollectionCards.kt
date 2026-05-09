package ru.mareanexx.collections_impl.presentation.screen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ru.mareanexx.collections_impl.R
import ru.mareanexx.collections_impl.presentation.model.Collection
import ru.mareanexx.ui.theme.backgroundMain
import ru.mareanexx.ui.theme.backgroundSecondary
import ru.mareanexx.ui.theme.bigCardBackground
import ru.mareanexx.ui.theme.bodySecondary
import ru.mareanexx.ui.theme.cardBookCounter
import ru.mareanexx.ui.theme.favoriteStar
import ru.mareanexx.ui.theme.onBackgroundMain
import ru.mareanexx.ui.theme.outerShadow
import ru.mareanexx.ui.theme.titleMain
import ru.mareanexx.ui.utils.GlobalUiConsts.HORIZONTAL_PADDING
import ru.mareanexx.common_ui.R as CommonUiR

@Composable
fun BigCollectionCard(
    collection: Collection.Favorite,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(horizontal = HORIZONTAL_PADDING)
            .wrapContentHeight()
            .fillMaxWidth()
            .shadow(elevation = 5.dp, shape = RoundedCornerShape(28.dp))
            .border(width = 3.dp, color = backgroundMain, shape = RoundedCornerShape(28.dp))
            .background(color = bigCardBackground, shape = RoundedCornerShape(28.dp)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            collection.icon?.let {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    tint = collection.tintColor ?: favoriteStar,
                    modifier = Modifier.size(54.dp),
                )

                Text(
                    text = stringResource(collection.nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = onBackgroundMain,
                )

                Text(
                    text = collection.booksNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = onBackgroundMain,
                )

                Spacer(modifier = Modifier.height(16.dp))

                FilledTonalButton(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = backgroundMain,
                    ),
                    contentPadding = PaddingValues(start = 16.dp, end = 12.dp),
                    onClick = {  }, // TODO:
                    content = {
                        Text(
                            text = stringResource(CommonUiR.string.open_collection_button_txt),
                            style = MaterialTheme.typography.labelMedium,
                            color = bodySecondary,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            painter = painterResource(CommonUiR.drawable.arrow_right_alt_icon),
                            tint = bodySecondary,
                            contentDescription = null,
                        )
                    }
                )
            }
        }

        Image(
            contentScale = ContentScale.FillHeight,
            painter = painterResource(R.drawable.favorite_collection_bg_image),
            contentDescription = null,
        )
    }
}

@Composable
fun SmallCollectionCardsGrid(
    collections: List<Collection.UserCustom>,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = HORIZONTAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier,
    ) {
        items(collections) { collection ->
            SmallCollectionCard(
                collection = collection,
            )
        }
    }
}

@Composable
fun SmallCollectionCard(
    collection: Collection.UserCustom,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = outerShadow, spotColor = outerShadow,
            )
            .fillMaxWidth()
            .background(
                color = backgroundSecondary,
                shape = RoundedCornerShape(16.dp),
            ),
    ) {
        Box {
            AsyncImage(
                model = collection.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.collection_1_example),
            )
            Image( // TODO: заменить на AsyncImage
                painter = painterResource(R.drawable.collection_1_example),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = outerShadow, spotColor = outerShadow,
                    )
                    .size(28.dp)
                    .background(
                        color = backgroundSecondary,
                        shape = RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(CommonUiR.drawable.more_horiz_icon),
                    tint = titleMain,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = collection.name,
                style = MaterialTheme.typography.bodyMedium,
                color = titleMain,
            )
            Text(
                text = collection.booksNumber,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 10.sp,
                color = cardBookCounter,
            )
        }
    }
}
