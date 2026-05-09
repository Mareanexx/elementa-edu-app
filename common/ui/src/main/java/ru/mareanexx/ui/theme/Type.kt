package ru.mareanexx.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.mareanexx.common_ui.R

@OptIn(ExperimentalTextApi::class)
val SemiBoldNunito = FontFamily(
    Font(
        resId = R.font.nunito_variable_font,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.SemiBold.weight),
        ),
    ),
)

@OptIn(ExperimentalTextApi::class)
val BoldNunito = FontFamily(
    Font(
        resId = R.font.nunito_variable_font,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Bold.weight),
        ),
    ),
)

@OptIn(ExperimentalTextApi::class)
val ExtraBoldNunito = FontFamily(
    Font(
        resId = R.font.nunito_variable_font,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.ExtraBold.weight),
        ),
    ),
)

@OptIn(ExperimentalTextApi::class)
val NormalNunito = FontFamily(
    Font(
        resId = R.font.nunito_variable_font,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
        ),
    ),
)

@OptIn(ExperimentalTextApi::class)
val MediumNunito = FontFamily(
    Font(
        resId = R.font.nunito_variable_font,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Medium.weight),
        ),
    ),
)

val fontFamily = FontFamily(
    Font(resId = R.font.nunito_variable_font, weight = FontWeight.Light),
    Font(resId = R.font.nunito_variable_font, weight = FontWeight.Normal),
    Font(resId = R.font.nunito_variable_font, weight = FontWeight.Medium),
    Font(resId = R.font.nunito_variable_font, weight = FontWeight.SemiBold),
    Font(resId = R.font.nunito_variable_font, weight = FontWeight.Bold),
    Font(resId = R.font.nunito_variable_font, weight = FontWeight.ExtraBold),
)

val ElementaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = ExtraBoldNunito,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = BoldNunito,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = ExtraBoldNunito,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 20.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = MediumNunito,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = MediumNunito,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = BoldNunito,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
    )
)
