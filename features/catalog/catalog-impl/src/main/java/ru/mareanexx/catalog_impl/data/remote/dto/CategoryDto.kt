package ru.mareanexx.catalog_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class CategoryDto(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    @SerializedName("icon_key") val iconKey: String? = null,
    @SerializedName("book_count") val bookCount: Int? = null,
)
