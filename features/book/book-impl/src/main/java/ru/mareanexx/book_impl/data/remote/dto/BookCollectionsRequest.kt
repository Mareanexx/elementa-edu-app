package ru.mareanexx.book_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class BookCollectionsRequest(
    @SerializedName("collection_ids") val ids: List<String>,
)
