package ru.mareanexx.book_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class BookReviewDto(
    val id: String?,
    @SerializedName("author_id") val authorId: String?,
    @SerializedName("author_name") val authorName: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    val rating: Int?,
    val text: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    val likes: Int?,
    val dislikes: Int?,
    val reaction: String?,
    @SerializedName("reply_count") val replyCount: Int?,
    @SerializedName("is_mine") val isMine: Boolean?,
)
