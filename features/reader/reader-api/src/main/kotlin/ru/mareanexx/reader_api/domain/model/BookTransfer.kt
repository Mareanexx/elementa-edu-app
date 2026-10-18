package ru.mareanexx.reader_api.domain.model

data class BookTransfer(
    val bookId: String,
    val title: String,
    val phase: TransferPhase,
    val pinned: Boolean,
    val bytesRead: Long,
    val totalBytes: Long,
    val error: ReaderError? = null,
) {
    val active: Boolean get() = phase in listOf(TransferPhase.Queued, TransferPhase.Downloading, TransferPhase.Preparing)
    val progress: Float get() = if (totalBytes > 0) (bytesRead.toDouble() / totalBytes).toFloat().coerceIn(0f, 1f) else 0f
}
