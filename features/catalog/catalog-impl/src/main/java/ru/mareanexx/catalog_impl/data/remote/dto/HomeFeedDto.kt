package ru.mareanexx.catalog_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class HomeFeedDto(
    @SerializedName("continue_reading") val continueReading: List<CatalogBookDto>? = null,
    val categories: List<CategoryDto>? = null,
    val sections: List<HomeSectionDto>? = null,
    @SerializedName("library_count") val libraryCount: Int? = null,
    @SerializedName("collection_count") val collectionCount: Int? = null,
    val unread: UnreadDto? = null,
)
