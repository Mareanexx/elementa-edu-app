package ru.mareanexx.book_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class BookReviewRequest(
    val rating: Int,
    val text: String,
)
