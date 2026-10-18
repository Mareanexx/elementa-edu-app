package ru.mareanexx.reader_impl.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_impl.R

@Composable
fun readerErrorText(error: ReaderError): String = stringResource(when (error) {
    ReaderError.Offline -> R.string.reader_error_offline
    ReaderError.Timeout -> R.string.reader_error_timeout
    ReaderError.Unauthorized -> R.string.reader_error_auth
    ReaderError.Forbidden -> R.string.reader_error_forbidden
    ReaderError.NotFound -> R.string.reader_error_missing
    ReaderError.UnsupportedFormat -> R.string.reader_error_format
    ReaderError.CorruptBook -> R.string.reader_error_corrupt
    ReaderError.TooLarge -> R.string.reader_error_large
    ReaderError.OutOfSpace -> R.string.reader_error_space
    ReaderError.Storage -> R.string.reader_error_storage
    ReaderError.Cancelled -> R.string.reader_error_cancelled
    ReaderError.InvalidAnnotation -> R.string.reader_error_note
    else -> R.string.reader_error_server
})
fun themeText(theme: ReaderTheme) = when (theme) { ReaderTheme.Light -> R.string.reader_theme_light; ReaderTheme.Paper -> R.string.reader_theme_paper; ReaderTheme.Sepia -> R.string.reader_theme_sepia; ReaderTheme.Night -> R.string.reader_theme_night }
fun fontText(font: ReaderFont) = when (font) { ReaderFont.Nunito -> R.string.reader_font_nunito; ReaderFont.Serif -> R.string.reader_font_serif; ReaderFont.Sans -> R.string.reader_font_sans }
fun colorText(color: HighlightColor) = when (color) { HighlightColor.Amber -> R.string.reader_color_amber; HighlightColor.Mint -> R.string.reader_color_mint; HighlightColor.Sky -> R.string.reader_color_sky; HighlightColor.Lavender -> R.string.reader_color_lavender; HighlightColor.Rose -> R.string.reader_color_rose; HighlightColor.Peach -> R.string.reader_color_peach }
