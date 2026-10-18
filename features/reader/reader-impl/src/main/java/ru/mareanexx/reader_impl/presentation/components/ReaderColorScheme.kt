package ru.mareanexx.reader_impl.presentation.components

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import ru.mareanexx.reader_api.domain.model.ReaderTheme

/** Ридер использует собственную нейтральную палитру, включая стандартные элементы Material. */
fun readerColorScheme(theme: ReaderTheme): androidx.compose.material3.ColorScheme {
    val palette = readerPalette(theme)
    val base = if (theme == ReaderTheme.Night) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = palette.accent,
        onPrimary = palette.surface,
        primaryContainer = palette.paper,
        onPrimaryContainer = palette.ink,
        inversePrimary = palette.muted,
        secondary = palette.accent,
        onSecondary = palette.surface,
        secondaryContainer = palette.paper,
        onSecondaryContainer = palette.ink,
        tertiary = palette.accent,
        onTertiary = palette.surface,
        tertiaryContainer = palette.paper,
        onTertiaryContainer = palette.ink,
        background = palette.paper,
        onBackground = palette.ink,
        surface = palette.surface,
        onSurface = palette.ink,
        surfaceVariant = palette.paper,
        onSurfaceVariant = palette.muted,
        surfaceTint = palette.accent,
        outline = palette.muted.copy(alpha = .4f),
        outlineVariant = palette.muted.copy(alpha = .15f),
        inverseSurface = palette.ink,
        inverseOnSurface = palette.paper,
        surfaceDim = palette.paper,
        surfaceBright = palette.surface,
        surfaceContainerLowest = palette.surface,
        surfaceContainerLow = palette.surface,
        surfaceContainer = palette.paper,
        surfaceContainerHigh = palette.paper,
        surfaceContainerHighest = palette.paper,
    )
}
