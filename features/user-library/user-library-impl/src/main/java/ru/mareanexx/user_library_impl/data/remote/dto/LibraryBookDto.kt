package ru.mareanexx.user_library_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class LibraryBookDto(
    val id: String?,
    val title: String?,
    val authors: String?,
    val pages: Int?,
    @SerializedName("pages_read") val pagesRead: Int?,
    @SerializedName("reading_status") val status: String?,
    @SerializedName("cover_url") val coverUrl: String?,
    @SerializedName("cover_key") val coverKey: String?,
    val rating: LibraryRatingDto?,
    val tags: List<LibraryTagDto>?,
    val favorite: Boolean?,
    @SerializedName("want_to_read") val wantToRead: Boolean?,
    @SerializedName("collection_count") val collectionCount: Int?,
)
