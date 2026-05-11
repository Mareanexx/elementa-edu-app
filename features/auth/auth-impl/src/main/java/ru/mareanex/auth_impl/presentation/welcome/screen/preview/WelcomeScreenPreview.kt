package ru.mareanex.auth_impl.presentation.welcome.screen.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9_PRO
import androidx.compose.ui.tooling.preview.Preview
import ru.mareanex.auth_impl.presentation.welcome.screen.WelcomeScreen
import ru.mareanexx.ui.theme.ElementaAppTheme

@Preview(device = PIXEL_9_PRO)
@Composable
private fun WelcomeScreenPreview() {
    ElementaAppTheme {
        WelcomeScreen()
    }
}
