package ru.mareanexx.user_profile_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class StatisticsDto(
    @SerializedName("books_read") val booksRead: Int?,
    @SerializedName("notes_published") val notesPublished: Int?,
    @SerializedName("tests_passed") val testsPassed: Int?,
)
