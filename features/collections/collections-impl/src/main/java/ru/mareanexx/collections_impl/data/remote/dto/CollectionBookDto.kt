package ru.mareanexx.collections_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CollectionBookDto(
    val id: String?,
    val title: String?,
    val authors: String?,
    val description: String?,
    val pages: Int?,
    @SerializedName("cover_key") val coverKey: String?,
    @SerializedName("cover_url") val coverUrl: String? = null,
    val rating: BookRatingDto? = null,
    val tags: List<BookTagDto?>? = null,
    @SerializedName("pages_read") val pagesRead: Int? = null,
)
