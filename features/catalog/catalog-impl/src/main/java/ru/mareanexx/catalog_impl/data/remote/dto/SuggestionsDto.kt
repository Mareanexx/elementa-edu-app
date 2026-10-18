package ru.mareanexx.catalog_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class SuggestionsDto(
    val queries: List<String>? = null,
)
