package ru.mareanexx.reader_api.domain.model

data class ReaderDocument(
    val book: ReaderBook,
    val version: String,
    val directory: String,
    val chapters: List<ReaderChapter>,
    val position: ReaderPosition?,
)
