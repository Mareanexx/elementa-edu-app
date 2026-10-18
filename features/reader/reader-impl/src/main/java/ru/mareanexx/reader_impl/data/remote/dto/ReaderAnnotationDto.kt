package ru.mareanexx.reader_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class ReaderAnnotationDto(
    val id: String? = null,
    @SerializedName("book_id") val bookId: String? = null,
    val anchor: ReaderAnchorDto? = null,
    val color: String? = null,
    val note: String? = null,
    @SerializedName("created_at") val createdAt: Long? = null,
    @SerializedName("updated_at") val updatedAt: Long? = null,
    val revision: Long? = null,
    val deleted: Boolean? = null,
)
