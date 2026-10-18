package ru.mareanexx.reader_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class ReaderAnchorDto(
    val version: String? = null,
    @SerializedName("chapter_id") val chapterId: String? = null,
    val start: Int? = null,
    val end: Int? = null,
    val quote: String? = null,
    val prefix: String? = null,
    val suffix: String? = null,
)
