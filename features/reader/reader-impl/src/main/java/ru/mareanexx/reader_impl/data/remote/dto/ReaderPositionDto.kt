package ru.mareanexx.reader_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class ReaderPositionDto(
    @SerializedName("book_id") val bookId: String? = null,
    val version: String? = null,
    @SerializedName("chapter_id") val chapterId: String? = null,
    val offset: Int? = null,
    val progression: Double? = null,
    @SerializedName("source_pages_read") val sourcePagesRead: Int? = null,
    @SerializedName("updated_at") val updatedAt: Long? = null,
)
