package ru.mareanexx.reader_impl.presentation.model

import ru.mareanexx.reader_api.domain.model.ReaderAnchor

data class ReaderSelection(
    val anchor: ReaderAnchor,
    val action: String,
)
