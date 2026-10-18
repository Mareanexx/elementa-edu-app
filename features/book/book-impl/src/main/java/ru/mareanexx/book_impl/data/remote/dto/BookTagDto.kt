package ru.mareanexx.book_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class BookTagDto(
    val id: String?,
    val label: String?,
    val kind: String?,
)
