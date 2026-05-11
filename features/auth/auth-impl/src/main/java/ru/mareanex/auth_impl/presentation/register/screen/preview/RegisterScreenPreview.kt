package ru.mareanex.auth_impl.presentation.register.screen.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.mareanex.auth_impl.presentation.register.screen.RegisterScreen
import ru.mareanexx.ui.theme.ElementaAppTheme

@Preview
@Composable
fun RegisterScreenPreview() {
    ElementaAppTheme {
        RegisterScreen(onBackClick = {})
    }
}
