package ru.mareanex.auth_impl.presentation.auth.screen.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.mareanexx.ui.modifiers.elementaShadow
import ru.mareanexx.ui.theme.backgroundMain
import ru.mareanexx.ui.theme.bodySecondary
import ru.mareanexx.ui.theme.titleMain
import ru.mareanexx.common_ui.R as CommonUiR

@Composable
fun AnotherWayToLogInBlock(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(CommonUiR.string.another_way_to_login),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = titleMain,
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AnotherLogInWay(icon = CommonUiR.drawable.public_icon)
            AnotherLogInWay(icon = CommonUiR.drawable.public_icon)
            AnotherLogInWay(icon = CommonUiR.drawable.public_icon)
        }
    }
}

@Composable
fun AnotherLogInWay(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .elementaShadow()
            .border(width = 2.dp, color = bodySecondary, shape = RoundedCornerShape(16.dp))
            .background(color = backgroundMain, shape = RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            tint = bodySecondary,
            contentDescription = null,
            modifier = Modifier.size(34.dp),
        )
    }
}
