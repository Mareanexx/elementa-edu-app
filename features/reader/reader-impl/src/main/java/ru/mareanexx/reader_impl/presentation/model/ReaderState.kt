package ru.mareanexx.reader_impl.presentation.model

import ru.mareanexx.reader_api.domain.model.*

data class ReaderState(
    val attempt: Int = 0,
    val loading: Boolean = true,
    val rendering: Boolean = true,
    val document: ReaderDocument? = null,
    val transfer: BookTransfer? = null,
    val error: ReaderError? = null,
    val operationError: ReaderError? = null,
    val settings: ReaderSettings = ReaderSettings(),
    val annotations: List<ReaderAnnotation> = emptyList(),
    val sync: ReaderSyncState = ReaderSyncState(),
    val chrome: Boolean = true,
    val panel: ReaderPanel = ReaderPanel.None,
    val selection: ReaderSelection? = null,
    val selectedAnnotation: ReaderAnnotation? = null,
    val note: ReaderNoteDraft? = null,
    val confirmDiscard: Boolean = false,
    val page: Int = 1,
    val pages: Int = 1,
    val chapter: String = "",
    val position: ReaderPosition? = null,
    val atEnd: Boolean = false,
    val completed: Boolean = false,
    val completion: ReaderCompletion = ReaderCompletion.Hidden,
    val completionError: ReaderError? = null,
)
