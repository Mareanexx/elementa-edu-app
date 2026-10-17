package ru.mareanexx.user_profile_impl.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.mareanexx.ui.theme.*
import ru.mareanexx.user_profile_impl.R

/** Многострочный текст остаётся по центру поля при любой длине описания. */
@Composable
internal fun ProfileBioField(
    value: String,
    label: String,
    enabled: Boolean,
    focused: Boolean,
    errorText: String?,
    modifier: Modifier,
    onValueChange: (String) -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        maxLines = 6,
        textStyle = MaterialTheme.typography.labelLarge.copy(color = titleMain, textAlign = TextAlign.Start),
        cursorBrush = SolidColor(bodySecondary),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = modifier.clip(shape).background(authFieldBackground)
            .border(2.dp, when { errorText != null -> Color(0xFFB4485D); focused -> bodySecondary; else -> Color.Transparent }, shape)
            .semantics { contentDescription = label; if (errorText != null) error(errorText) },
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth().heightIn(min = 116.dp).padding(16.dp), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) Text(stringResource(R.string.profile_bio_hint), color = username, style = MaterialTheme.typography.labelLarge)
                inner()
            }
        },
    )
    if (errorText != null) Text(errorText, color = Color(0xFFB4485D), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
}
