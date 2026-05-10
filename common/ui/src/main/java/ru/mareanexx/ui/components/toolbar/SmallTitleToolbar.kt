package ru.mareanexx.ui.components.toolbar

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
import androidx.compose.ui.unit.dp
import ru.mareanexx.ui.components.toolbar.model.SmallTitleToolbarActionButton
import ru.mareanexx.ui.modifiers.elementaClickable
import ru.mareanexx.ui.theme.titleMain
import ru.mareanexx.common_ui.R as CommonUiR

/**
 * Тулбар небольшого размера с текстом посередине, кнопкой "Назад" слева и
 * экшн кнопкой справа
 */
@Composable
fun SmallTitleToolbar(
    @StringRes title: Int,
    onBackClick: () -> Unit,
    actionButton: SmallTitleToolbarActionButton? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(CommonUiR.drawable.arrow_back_icon),
            tint = titleMain,
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .elementaClickable(isRippleEnabled = false) {
                    onBackClick()
                },
        )

        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.displaySmall,
            color = titleMain,
            maxLines = 1,
        )

        actionButton?.let { button ->
            Icon(
                painter = painterResource(button.iconRes),
                tint = titleMain,
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .elementaClickable(isRippleEnabled = false) {
                        button.onClick()
                    },
            )
        }
    }
}
