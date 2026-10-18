package ru.mareanexx.reader_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class ReaderAnnotationsPageDto(
    val items: List<ReaderAnnotationDto>? = null,
    @SerializedName("next_cursor") val nextCursor: String? = null,
)
