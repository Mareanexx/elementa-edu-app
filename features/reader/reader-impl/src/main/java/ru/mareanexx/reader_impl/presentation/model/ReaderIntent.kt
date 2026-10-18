package ru.mareanexx.reader_impl.presentation.model

import ru.mareanexx.reader_api.domain.model.*

sealed interface ReaderIntent {
    data object Retry : ReaderIntent
    data object Ready : ReaderIntent
    data object RenderFailed : ReaderIntent
    data object ToggleChrome : ReaderIntent
    data class Panel(
        val value: ReaderPanel,
    ) : ReaderIntent
    data class Settings(
        val value: ReaderSettings,
    ) : ReaderIntent
    data class Position(
        val offset: Int,
        val progression: Double,
        val chapterId: String,
        val chapter: String,
        val page: Int,
        val pages: Int,
        val atEnd: Boolean = false,
    ) : ReaderIntent
    data class Selection(
        val value: ReaderSelection,
    ) : ReaderIntent
    data class Annotation(
        val id: String,
    ) : ReaderIntent
    data class Color(
        val value: HighlightColor,
    ) : ReaderIntent
    data object EditNote : ReaderIntent
    data class NoteText(
        val value: String,
    ) : ReaderIntent
    data object SaveNote : ReaderIntent
    data object DismissNote : ReaderIntent
    data object DiscardNote : ReaderIntent
    data object KeepNote : ReaderIntent
    data object DeleteAnnotation : ReaderIntent
    data object DismissError : ReaderIntent
    data object Flush : ReaderIntent
    data object CompleteBook : ReaderIntent
    data object DismissCompletion : ReaderIntent
}
