package ru.mareanexx.book_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class BookRatingDto(
    val average: Double?,
    val count: Int?,
    val distribution: List<Int>?,
)
