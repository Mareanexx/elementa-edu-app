package ru.mareanexx.catalog_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class HomeSectionDto(
    val id: String? = null,
    val books: List<CatalogBookDto>? = null,
    val sort: String? = null,
)
