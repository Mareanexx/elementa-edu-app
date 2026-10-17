package ru.mareanexx.ui.components.text_fields

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import ru.mareanexx.ui.theme.titleMain
import ru.mareanexx.ui.theme.username

/**
 * Текстовое поле с режимом ввода
 */
@Composable
fun ElementaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    @DrawableRes leadingIcon: Int,
    @StringRes placeholderText: Int,
    trailingIcon: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    containerColor: Color = Color.Transparent,
    unfocusedBorderColor: Color = username,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Placeholder(value = value, text = placeholderText) },
        leadingIcon = { LeadingIcon(leadingIcon) },
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        enabled = enabled,
        isError = error != null,
        supportingText = error?.let { message -> {
            Text(message, color = Color(0xFFB4485D),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, lineHeight = 16.sp))
        } },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = modifier,
        textStyle = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Medium,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledContainerColor = containerColor,
            errorContainerColor = containerColor,
            errorBorderColor = Color(0xFFB4485D),
            errorLabelColor = Color(0xFFB4485D),
            errorLeadingIconColor = Color(0xFFB4485D),
            errorTrailingIconColor = Color(0xFFB4485D),
            unfocusedBorderColor = unfocusedBorderColor,
            focusedBorderColor = titleMain,
            unfocusedLeadingIconColor = username,
            focusedLeadingIconColor = titleMain,
            unfocusedTrailingIconColor = username,
            focusedTrailingIconColor = titleMain,
            focusedTextColor = titleMain,
            unfocusedTextColor = titleMain,
            focusedLabelColor = titleMain,
            unfocusedLabelColor = username,
        ),
    )
}

@Composable
private fun LeadingIcon(
    @DrawableRes iconRes: Int,
) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier
            .padding(start = 10.dp)
            .size(28.dp),
    )
}

@Composable
fun TrailingIcon(
    @DrawableRes iconRes: Int,
) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier
            .padding(end = 10.dp)
            .size(28.dp),
    )
}

@Composable
private fun Placeholder(
    value: String,
    @StringRes text: Int,
) {
    Text(
        text = stringResource(text),
        style = if (value.isBlank()) {
            MaterialTheme.typography.displaySmall
        } else {
            MaterialTheme.typography.bodyMedium
        },
        fontWeight = FontWeight.Medium,
        color = username,
    )
}
