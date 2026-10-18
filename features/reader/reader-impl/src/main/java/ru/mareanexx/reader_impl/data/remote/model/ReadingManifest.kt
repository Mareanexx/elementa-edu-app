package ru.mareanexx.reader_impl.data.remote.model

import ru.mareanexx.reader_api.domain.model.ReaderBook

internal data class ReadingManifest(
    val book: ReaderBook,
    val version: String,
    val contentUrl: String,
    val byteSize: Long,
    val sha256: String,
)
