package com.example.book_impl.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_impl.presentation.model.BookTag
import ru.mareanexx.ui.theme.SemiBoldNunito

@Composable
fun TagsGrid(
    tags: List<BookTag>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        maxLines = 3,
        modifier = modifier.fillMaxWidth(),
    ) {
        tags.forEach { tag ->
            BookTagBlock(tag = tag)
        }
    }
}

@Composable
private fun BookTagBlock(
    tag: BookTag,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(22.dp)
            .wrapContentWidth()
            .background(
                color = tag.backgroundColor,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = tag.name,
            fontFamily = SemiBoldNunito,
            fontSize = 12.sp,
            lineHeight = 12.sp,
            letterSpacing = 0.sp,
            fontWeight = FontWeight.Medium,
            color = tag.textColor,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            softWrap = false,
        )
    }
}
