package ru.mareanexx.book_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class BookShelvesDto(
    val favorite: Boolean?,
    @SerializedName("want_to_read") val wantToRead: Boolean?,
    @SerializedName("collection_count") val collectionCount: Int?,
)
