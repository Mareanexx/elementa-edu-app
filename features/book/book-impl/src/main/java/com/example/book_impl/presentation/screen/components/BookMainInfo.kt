package com.example.book_impl.presentation.screen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_impl.presentation.model.Book
import ru.mareanexx.book_impl.R
import ru.mareanexx.ui.components.content_header.BlockContentHeader
import ru.mareanexx.ui.theme.MediumNunito
import ru.mareanexx.ui.theme.authorsText
import ru.mareanexx.ui.theme.titleMain
import ru.mareanexx.common_ui.R as CommonUiR

@Composable
fun BookInfo(
    book: Book,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
    ) {
        // AsyncImage( // TODO: заменить на AsyncImage
        //     model = book.imagePath,
        //     contentScale = ContentScale.Crop,
        //     contentDescription = null,
        //     modifier = Modifier
        //         .width(156.dp),
        //         .clip(shape = RoundedCornerShape(8.dp))
        // )
        Image(
            painter = painterResource(R.drawable.book_example),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier
                .width(156.dp)
                .clip(shape = RoundedCornerShape(8.dp)),
        )

        Column(
            modifier = Modifier
                .padding(start = 18.dp, top = 8.dp, bottom = 8.dp),
        ) {
            TagsGrid(tags = book.tags)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = book.title,
                style = MaterialTheme.typography.displayMedium,
                fontSize = 22.sp,
                color = titleMain,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = book.authors,
                style = MaterialTheme.typography.labelMedium,
                color = authorsText,
            )
        }
    }
}

@Composable
fun BookDescription(
    description: String,
    onShowFullClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        BlockContentHeader(
            titleRes = CommonUiR.string.block_description_header,
            buttonText = CommonUiR.string.full_description_button_txt,
            buttonIcon = CommonUiR.drawable.keyboard_arrow_right_icon,
            onButtonClick = onShowFullClick,
            horizontalPaddings = PaddingValues(0.dp),
        )

        Text(
            text = description,
            style = MaterialTheme.typography.displaySmall,
            fontFamily = MediumNunito,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            color = titleMain,
            maxLines = 6,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
