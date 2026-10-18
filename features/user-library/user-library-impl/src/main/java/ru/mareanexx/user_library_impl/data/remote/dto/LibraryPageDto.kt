package ru.mareanexx.user_library_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class LibraryPageDto(
    val items: List<LibraryBookDto>?,
    @SerializedName("next_cursor") val nextCursor: String?,
    @SerializedName("total_count") val totalCount: Int?,
    val counts: LibraryCountsDto?,
    val highlights: LibraryHighlightsDto?,
)
