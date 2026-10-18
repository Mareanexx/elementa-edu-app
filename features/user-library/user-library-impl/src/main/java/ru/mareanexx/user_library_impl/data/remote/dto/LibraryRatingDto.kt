package ru.mareanexx.user_library_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class LibraryRatingDto(
    val average: Double?,
    val count: Int?,
)
