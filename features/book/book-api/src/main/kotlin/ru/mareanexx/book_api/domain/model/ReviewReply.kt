package ru.mareanexx.book_api.domain.model

data class ReviewReply(
    val id: String,
    val authorName: String,
    val avatarUrl: String?,
    val text: String,
    val createdAt: Long,
    val isMine: Boolean,
)
