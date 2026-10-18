package ru.mareanexx.book_impl.data.remote.dto

import com.google.gson.annotations.SerializedName

internal data class BookReplyDto(
    val id: String?,
    @SerializedName("author_name") val authorName: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    val text: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("is_mine") val isMine: Boolean?,
)
