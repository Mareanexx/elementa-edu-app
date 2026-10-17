package ru.mareanexx.collections_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CollectionBooksRequest(
    @SerializedName("book_ids") val bookIds: List<String>?,
)
