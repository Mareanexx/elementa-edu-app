package ru.mareanexx.user_library_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class LibraryCountsDto(
    val all: Int?,
    @SerializedName("in_progress") val inProgress: Int?,
    val completed: Int?,
    @SerializedName("want_to_read") val wantToRead: Int?,
)
