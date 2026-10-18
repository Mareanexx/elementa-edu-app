package ru.mareanexx.book_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class BookCollectionChoiceDto(
    val id: String?,
    val name: String?,
    @SerializedName("book_count") val bookCount: Int?,
    val selected: Boolean?,
)
