package ru.mareanexx.reader_api.domain.model

data class OfflineBook(
    val book: ReaderBook,
    val pagesRead: Int,
    val sizeBytes: Long,
    val downloadedAt: Long,
)
