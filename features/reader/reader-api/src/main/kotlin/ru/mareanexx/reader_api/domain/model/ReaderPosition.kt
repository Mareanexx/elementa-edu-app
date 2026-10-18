package ru.mareanexx.reader_api.domain.model

data class ReaderPosition(
    val bookId: String,
    val version: String,
    val chapterId: String,
    val offset: Int,
    val progression: Double,
    val sourcePagesRead: Int,
    val updatedAt: Long,
)
