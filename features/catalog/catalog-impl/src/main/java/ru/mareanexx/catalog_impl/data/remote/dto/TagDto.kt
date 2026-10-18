package ru.mareanexx.catalog_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class TagDto(
    val id: String? = null,
    val label: String? = null,
    val kind: String? = null,
)
