package ru.mareanexx.book_api.domain.model

data class RepliesPage(
    val items: List<ReviewReply>,
    val nextCursor: String?,
    val totalCount: Int,
)
