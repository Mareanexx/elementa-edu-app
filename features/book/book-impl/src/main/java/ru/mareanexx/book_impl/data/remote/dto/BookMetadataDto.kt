package ru.mareanexx.book_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class BookMetadataDto(
    val publisher: String?,
    val year: Int?,
    val language: String?,
    val isbn: String?,
    val format: String?,
)
