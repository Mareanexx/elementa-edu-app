package ru.mareanexx.reader_api.domain.model

data class ReaderSyncState(
    val pending: Int = 0,
    val syncing: Boolean = false,
    val error: ReaderError? = null,
)
