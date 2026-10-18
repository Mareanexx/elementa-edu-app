package ru.mareanexx.book_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class BookDetailsDto(
    val id: String?,
    val title: String?,
    val authors: String?,
    val description: String?,
    val images: List<BookImageDto>?,
    val tags: List<BookTagDto>?,
    val pages: Int?,
    @SerializedName("pages_read") val pagesRead: Int?,
    @SerializedName("file_bytes") val fileBytes: Long?,
    @SerializedName("notes_count") val notesCount: Int?,
    val rating: BookRatingDto?,
    val shelves: BookShelvesDto?,
    val metadata: BookMetadataDto?,
    @SerializedName("my_review") val myReview: BookReviewDto?,
    @SerializedName("preview_reviews") val previewReviews: List<BookReviewDto>?,
)
