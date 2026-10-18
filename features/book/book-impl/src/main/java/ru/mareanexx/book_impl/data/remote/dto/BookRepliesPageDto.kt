package ru.mareanexx.book_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class BookRepliesPageDto(
    val items: List<BookReplyDto>?,
    @SerializedName("next_cursor") val nextCursor: String?,
    @SerializedName("total_count") val totalCount: Int?,
)
