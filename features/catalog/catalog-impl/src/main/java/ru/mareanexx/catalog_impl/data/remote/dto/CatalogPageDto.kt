package ru.mareanexx.catalog_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class CatalogPageDto(
    val items: List<CatalogBookDto>? = null,
    @SerializedName("next_cursor") val nextCursor: String? = null,
    val total: Int? = null,
)
