package ru.mareanexx.book_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class BookReviewsPageDto(
    val items: List<BookReviewDto>?,
    @SerializedName("next_cursor") val nextCursor: String?,
    @SerializedName("total_count") val totalCount: Int?,
)
