package ru.mareanexx.catalog_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class CatalogBookDto(
    val id: String? = null,
    val title: String? = null,
    val authors: String? = null,
    val pages: Int? = null,
    @SerializedName("pages_read") val pagesRead: Int? = null,
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("cover_key") val coverKey: String? = null,
    val rating: RatingDto? = null,
    val tags: List<TagDto>? = null,
    val favorite: Boolean? = null,
    @SerializedName("want_to_read") val wantToRead: Boolean? = null,
    @SerializedName("collection_count") val collectionCount: Int? = null,
)
