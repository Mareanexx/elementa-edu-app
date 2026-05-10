package com.example.book_impl.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_impl.presentation.model.BookCharacteristic
import com.example.book_impl.presentation.model.BookCharacteristics
import ru.mareanexx.ui.theme.divider
import ru.mareanexx.ui.theme.titleMain

@Composable
fun BookCharacteristics(
    bookCharacteristics: BookCharacteristics,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Max),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BookCharacteristic(bookCharacteristics.rating)
        CharacteristicsDivider()
        BookCharacteristic(bookCharacteristics.size)
        CharacteristicsDivider()
        BookCharacteristic(bookCharacteristics.pages)
        CharacteristicsDivider()
        BookCharacteristic(bookCharacteristics.notes)
    }
}

@Composable
private fun CharacteristicsDivider(modifier: Modifier = Modifier) {
    VerticalDivider(modifier = modifier.height(30.dp), color = divider)
}

@Composable
private fun BookCharacteristic(
    characteristic: BookCharacteristic,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(75.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = characteristic.mainNumber,
                style = MaterialTheme.typography.titleSmall,
                color = titleMain,
                fontSize = 20.sp,
                lineHeight = 20.sp,
            )

            characteristic.iconRes?.let { iconRes ->
                Icon(
                    painter = painterResource(iconRes),
                    tint = titleMain,
                    contentDescription = null,
                )
            }
        }

        Text(
            text = characteristic.subtitle,
            color = titleMain,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 10.sp,
            textAlign = TextAlign.Center,
        )
    }
}
