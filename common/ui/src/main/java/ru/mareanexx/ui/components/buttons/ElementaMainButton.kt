package ru.mareanexx.ui.components.buttons

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.size
import ru.mareanexx.ui.theme.bodySecondary
import ru.mareanexx.ui.theme.onBackgroundMain

/**
 * Основная экшен кнопка
 *
 * @param textRes текст на кнопке
 * @param containerColor цвет контейнера кнопки
 * @param textColor цвет текста кнопки
 * @param onClick действие на клик по кнопке
 */
@Composable
fun ElementaMainButton(
    @StringRes textRes: Int,
    containerColor: Color = bodySecondary,
    textColor: Color = onBackgroundMain,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor.copy(alpha = 0.55f),
        ),
        contentPadding = PaddingValues(vertical = 14.dp),
        modifier = modifier,
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = textColor,
                strokeWidth = 2.dp)
        } else Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.titleSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
        )
    }
}
