package ru.mareanexx.reader_api.domain.model

data class ReaderAnnotation(
    val id: String,
    val bookId: String,
    val anchor: ReaderAnchor,
    val color: HighlightColor,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val revision: Long = 1,
    val deleted: Boolean = false,
)
