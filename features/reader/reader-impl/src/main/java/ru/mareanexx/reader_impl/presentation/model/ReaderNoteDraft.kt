package ru.mareanexx.reader_impl.presentation.model

import ru.mareanexx.reader_api.domain.model.*

data class ReaderNoteDraft(
    val anchor: ReaderAnchor,
    val annotationId: String? = null,
    val color: HighlightColor = HighlightColor.Amber,
    val original: String = "",
    val text: String = "",
    val saving: Boolean = false,
)
