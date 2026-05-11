package ru.mareanex.auth_impl.presentation.welcome.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mareanexx.auth_impl.R
import ru.mareanexx.ui.components.buttons.ElementaMainButton
import ru.mareanexx.ui.modifiers.elementaShadow
import ru.mareanexx.ui.theme.addToWantToReadBackground
import ru.mareanexx.ui.theme.backgroundMain
import ru.mareanexx.ui.theme.bodySecondary
import ru.mareanexx.ui.theme.titleMain
import ru.mareanexx.ui.theme.username
import ru.mareanexx.ui.utils.GlobalUiConsts.HORIZONTAL_PADDING
import ru.mareanexx.common_ui.R as CommonUiR

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = backgroundMain),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Image(
        //     painter = painterResource(R.drawable.elementa_welcome_title),
        //     contentDescription = null,
        //     modifier = Modifier.fillMaxWidth(fraction = 0.6f),
        // )
        Spacer(modifier = Modifier.height(30.dp))

        Image(
            painter = painterResource(R.drawable.welcome_screen_back),
            contentScale = ContentScale.FillWidth,
            contentDescription = null,
            modifier = Modifier.align(Alignment.End).fillMaxWidth(0.8f),
        )


        Spacer(modifier = Modifier.weight(0.5f))

        Column(
            modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING),
        ) {
            Text(
                text = "${stringResource(CommonUiR.string.app_name)}: ${stringResource(CommonUiR.string.welcome_screen_title)}",
                style = MaterialTheme.typography.displayLarge,
                fontSize = 36.sp,
                lineHeight = 36.sp,
                color = titleMain,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(CommonUiR.string.welcome_screen_subtitle),
                style = MaterialTheme.typography.labelLarge,
                color = username,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.weight(0.5f))

        Column(
            modifier = Modifier.padding(HORIZONTAL_PADDING),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ElementaMainButton(
                textRes = CommonUiR.string.signup_button_txt,
                onClick = { TODO() },
                modifier = Modifier.elementaShadow(
                    shape = RoundedCornerShape(100.dp),
                ).fillMaxWidth(),
            )

            ElementaMainButton(
                textRes = CommonUiR.string.login_button_txt,
                containerColor = addToWantToReadBackground,
                textColor = bodySecondary,
                onClick = { TODO() },
                modifier = Modifier.elementaShadow(
                    shape = RoundedCornerShape(100.dp),
                ).fillMaxWidth(),
            )
        }
    }
}
