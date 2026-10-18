package ru.mareanexx.reader_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class ReadingManifestDto(
    val book: ReaderBookDto? = null,
    val format: String? = null,
    val version: String? = null,
    @SerializedName("content_path") val contentPath: String? = null,
    @SerializedName("byte_size") val byteSize: Long? = null,
    val sha256: String? = null,
)
