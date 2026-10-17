package ru.mareanexx.collections_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CollectionDto(
    val id: String?,
    val name: String?,
    @SerializedName("cover_key") val coverKey: String?,
    @SerializedName("book_count") val bookCount: Int?,
    val kind: String?,
    @SerializedName("cover_image") val image: CollectionImageDto? = null,
)
