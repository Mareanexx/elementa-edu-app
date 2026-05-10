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
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
        ),
        contentPadding = PaddingValues(vertical = 14.dp),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.titleSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
        )
    }
}
