package ru.mareanexx.book_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class BookImageDto(
    val id: String?,
    val url: String?,
    val caption: String?,
)
