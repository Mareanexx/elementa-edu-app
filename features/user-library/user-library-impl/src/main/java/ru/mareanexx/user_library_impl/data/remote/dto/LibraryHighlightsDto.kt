package ru.mareanexx.user_library_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class LibraryHighlightsDto(
    @SerializedName("in_progress") val inProgress: List<LibraryBookDto>?,
    val completed: List<LibraryBookDto>?,
    @SerializedName("want_to_read") val wantToRead: List<LibraryBookDto>?,
)
