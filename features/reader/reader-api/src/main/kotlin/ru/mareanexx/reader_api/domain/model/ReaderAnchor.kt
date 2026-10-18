package ru.mareanexx.reader_api.domain.model

/** Смещения UTF-16 относятся к неизменной версии очищенного содержимого EPUB. */
data class ReaderAnchor(
    val version: String,
    val chapterId: String,
    val start: Int,
    val end: Int,
    val quote: String,
    val prefix: String,
    val suffix: String,
)
