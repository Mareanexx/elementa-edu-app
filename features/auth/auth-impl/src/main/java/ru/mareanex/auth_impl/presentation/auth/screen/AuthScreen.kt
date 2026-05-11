package ru.mareanex.auth_impl.presentation.auth.screen

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
import ru.mareanex.auth_impl.presentation.auth.screen.components.AnotherWayToLogInBlock
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
fun AuthScreen(
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundMain)
            .padding(horizontal = HORIZONTAL_PADDING),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
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

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(CommonUiR.string.screen_title_log_in),
                style = MaterialTheme.typography.headlineLarge,
                color = titleMain,
            )
            Spacer(modifier = Modifier.height(20.dp))

            LogInTextFieldsList(
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = stringResource(CommonUiR.string.forget_password),
                    style = MaterialTheme.typography.labelMedium,
                    color = username,
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            ElementaMainButton(
                textRes = CommonUiR.string.login_button_txt,
                onClick = { TODO() },
                modifier = Modifier
                    .elementaShadow(shape = RoundedCornerShape(100.dp))
                    .fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(14.dp))

            AnotherWayToLogInBlock()
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(CommonUiR.string.have_no_account_question),
                style = MaterialTheme.typography.displaySmall,
                color = username,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(CommonUiR.string.signup_button_txt),
                style = MaterialTheme.typography.displaySmall,
                color = bodySecondary,
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun LogInTextFieldsList(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
    }
}
