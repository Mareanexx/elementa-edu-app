package ru.mareanexx.ui.components.content_header

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.common_ui.R
import ru.mareanexx.ui.modifiers.elementaClickable
import ru.mareanexx.ui.theme.ElementaAppTheme
import ru.mareanexx.ui.theme.bodySecondary
import ru.mareanexx.ui.theme.titleMain
import ru.mareanexx.ui.utils.GlobalUiConsts.HORIZONTAL_PADDING

/**
 * Хедер для секции с контентом
 */
@Composable
fun BlockContentHeader(
    @StringRes titleRes: Int,
    @StringRes buttonText: Int,
    @DrawableRes buttonIcon: Int? = null,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 12.dp, start = HORIZONTAL_PADDING, end = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleSmall,
            color = titleMain,
        )

        Row(
            modifier = Modifier
                .elementaClickable(isRippleEnabled = false) {
                    onButtonClick()
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(buttonText),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 13.sp,
                color = bodySecondary,
            )

            buttonIcon?.let { iconRes ->
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = bodySecondary,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEEEDFD)
@Composable
private fun BlockContentHeaderPreview() {
    ElementaAppTheme {
        BlockContentHeader(
            titleRes = R.string.block_all_collections_header,
            buttonText = R.string.show_all,
            buttonIcon = R.drawable.keyboard_arrow_right_icon,
            onButtonClick = {},
        )
    }
}