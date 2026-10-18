package ru.mareanexx.catalog_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class RatingDto(
    val average: Double? = null,
    val count: Int? = null,
)
