package ru.mareanexx.collections_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CollectionsResponse(
    val items: List<CollectionDto>?,
)
