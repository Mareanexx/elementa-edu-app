package ru.mareanexx.book_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class BookCollectionsDto(
    val items: List<BookCollectionChoiceDto>?,
)
