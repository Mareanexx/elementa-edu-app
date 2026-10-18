package ru.mareanexx.reader_impl.presentation.components

import androidx.compose.ui.graphics.Color
import ru.mareanexx.reader_api.domain.model.*

data class ReaderPalette(
    val paper: Color,
    val ink: Color,
    val muted: Color,
    val surface: Color,
    val accent: Color,
)
fun readerPalette(theme: ReaderTheme) = when (theme) {
    ReaderTheme.Light -> ReaderPalette(Color(0xFFF4F4F4), Color(0xFF292834), Color(0xFF777382), Color.White, Color(0xFF48474C))
    ReaderTheme.Paper -> ReaderPalette(Color(0xFFFCF6EC), Color(0xFF423C35), Color(0xFF827461), Color(0xFFFFFBF5), Color(0xFF5D554B))
    ReaderTheme.Sepia -> ReaderPalette(Color(0xFFEAD4B5), Color(0xFF493B2C), Color(0xFF867055), Color(0xFFF4E5CD), Color(0xFF655447))
    ReaderTheme.Night -> ReaderPalette(Color(0xFF1D1D20), Color(0xFFE1DFE8), Color(0xFFAAA5B9), Color(0xFF303036), Color(0xFFE1DFE8))
}
fun highlightColor(color: HighlightColor) = when (color) {
    HighlightColor.Amber -> Color(0xFFF3D55B)
    HighlightColor.Mint -> Color(0xFF7DC9A8)
    HighlightColor.Sky -> Color(0xFF79BCE4)
    HighlightColor.Lavender -> Color(0xFFB19AE5)
    HighlightColor.Rose -> Color(0xFFE79EB6)
    HighlightColor.Peach -> Color(0xFFF1B17D)
}
