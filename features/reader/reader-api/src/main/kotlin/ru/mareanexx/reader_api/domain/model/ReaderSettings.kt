package ru.mareanexx.reader_api.domain.model

data class ReaderSettings(
    val theme: ReaderTheme = ReaderTheme.Light,
    val mode: ReadingMode = ReadingMode.Scroll,
    val font: ReaderFont = ReaderFont.Nunito,
    val fontSize: Float = 17f,
    val lineHeight: Float = 1.55f,
    val margin: Int = 20,
    val justify: Boolean = false,
    val showFormulas: Boolean = true,
    val showPageNumber: Boolean = true,
    val keepScreenOn: Boolean = true,
)
