package ru.mareanexx.collections_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CollectionPreferencesDto(
    @SerializedName("sort_order") val sortOrder: String?,
    @SerializedName("show_empty") val showEmpty: Boolean?,
)
