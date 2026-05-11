package ru.mareanex.auth_impl.presentation.register.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.mareanexx.ui.components.buttons.ElementaMainButton
import ru.mareanexx.ui.components.text_fields.ElementaTextField
import ru.mareanexx.ui.components.text_fields.TrailingIcon
import ru.mareanexx.ui.modifiers.elementaClickable
import ru.mareanexx.ui.modifiers.elementaShadow
import ru.mareanexx.ui.theme.backgroundMain
import ru.mareanexx.ui.theme.bodySecondary
import ru.mareanexx.ui.theme.titleMain
import ru.mareanexx.ui.theme.username
import ru.mareanexx.ui.utils.GlobalUiConsts.HORIZONTAL_PADDING
import ru.mareanexx.common_ui.R as CommonUiR

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundMain)
            .padding(horizontal = HORIZONTAL_PADDING),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                painter = painterResource(CommonUiR.drawable.arrow_back_icon),
                tint = titleMain,
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .elementaClickable(isRippleEnabled = false) {
                        onBackClick()
                    },
            )
        }

        Spacer(modifier = Modifier.weight(0.5f))
        Text(
            text = stringResource(CommonUiR.string.screen_title_create_account),
            style = MaterialTheme.typography.headlineLarge,
            color = titleMain,
        )
        Spacer(modifier = Modifier.height(20.dp))

        RegisterTextFieldsList(
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.weight(1f))

        ElementaMainButton(
            textRes = CommonUiR.string.signup_button_txt,
            onClick = { TODO() },
            modifier = Modifier
                .elementaShadow(shape = RoundedCornerShape(100.dp))
                .fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(CommonUiR.string.already_have_account_question),
                style = MaterialTheme.typography.displaySmall,
                color = username,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(CommonUiR.string.login_button_txt),
                style = MaterialTheme.typography.displaySmall,
                color = bodySecondary,
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun RegisterTextFieldsList(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ElementaTextField(
            value = "",
            onValueChange = { TODO() },
            leadingIcon = CommonUiR.drawable.person_icon,
            placeholderText = CommonUiR.string.firstname_and_lastname,
            modifier = Modifier.fillMaxWidth(),
        )
        ElementaTextField(
            value = "",
            onValueChange = { TODO() },
            leadingIcon = CommonUiR.drawable.alternate_email_icon,
            placeholderText = CommonUiR.string.username,
            modifier = Modifier.fillMaxWidth(),
        )
        ElementaTextField(
            value = "",
            onValueChange = { TODO() },
            leadingIcon = CommonUiR.drawable.password_icon,
            placeholderText = CommonUiR.string.password,
            trailingIcon = { TrailingIcon(CommonUiR.drawable.visibility_icon) },
            modifier = Modifier.fillMaxWidth(),
        )
        ElementaTextField(
            value = "",
            onValueChange = { TODO() },
            leadingIcon = CommonUiR.drawable.password_icon,
            placeholderText = CommonUiR.string.confirm_password,
            trailingIcon = { TrailingIcon(CommonUiR.drawable.visibility_icon) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
