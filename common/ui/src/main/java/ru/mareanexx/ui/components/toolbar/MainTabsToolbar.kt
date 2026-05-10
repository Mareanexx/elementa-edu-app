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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.mareanexx.common_ui.R
import ru.mareanexx.ui.components.toolbar.model.ToolbarButton
import ru.mareanexx.ui.modifiers.elementaClickable
import ru.mareanexx.ui.theme.ElementaAppTheme
import ru.mareanexx.ui.theme.titleMain

@Composable
fun MainTabsToolbar(
    @StringRes screenTitle: Int,
    additionalButtons: ToolbarButton.AdditionalButton?,
    settingsButton: ToolbarButton.SettingsButton,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(screenTitle),
            color = titleMain,
            modifier = Modifier.padding(vertical = 14.dp),
            style = MaterialTheme.typography.headlineLarge,
        )

        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            additionalButtons?.let { button ->
                Icon(
                    painter = painterResource(button.icon),
                    contentDescription = null,
                    tint = titleMain,
                    modifier = Modifier
                        .size(30.dp)
                        .elementaClickable(
                            isRippleEnabled = false,
                        ) { button.onClick() },
                )
            }
            Icon(
                painter = painterResource(settingsButton.icon),
                contentDescription = null,
                tint = titleMain,
                modifier = Modifier
                    .size(30.dp)
                    .elementaClickable(
                        isRippleEnabled = false,
                    ) { settingsButton.onClick() },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEEEDFD, device = Devices.PIXEL_9_PRO)
@Composable
fun MainTabsToolbarWithSettingsPreview() {
    ElementaAppTheme {
        MainTabsToolbar(
            screenTitle = R.string.screen_title_collections,
            additionalButtons = null,
            settingsButton = ToolbarButton.SettingsButton(
                icon = R.drawable.settings_icon,
                onClick = {},
            ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEEEDFD, device = Devices.PIXEL_9_PRO)
@Composable
fun MainTabsToolbarWithAdditionalButtonPreview() {
    ElementaAppTheme {
        MainTabsToolbar(
            screenTitle = R.string.screen_title_collections,
            additionalButtons = ToolbarButton.AdditionalButton(
                icon = R.drawable.add_icon,
                onClick = {},
            ),
            settingsButton = ToolbarButton.SettingsButton(
                icon = R.drawable.settings_icon,
                onClick = {},
            ),
        )
    }
}
