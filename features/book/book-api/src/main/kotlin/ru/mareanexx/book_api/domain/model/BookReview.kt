package ru.mareanexx.book_api.domain.model

data class BookReview(
    val id: String,
    val authorId: String,
    val authorName: String,
    val avatarUrl: String?,
    val rating: Int,
    val text: String,
    val createdAt: Long,
    val updatedAt: Long?,
    val likes: Int,
    val dislikes: Int,
    val reaction: ReviewReaction?,
    val replyCount: Int,
    val isMine: Boolean,
)
