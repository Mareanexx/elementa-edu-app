package ru.mareanexx.book_impl.data.mock

internal data class DemoStoredReply(
    val id: String,
    val authorId: String,
    val authorName: String,
    val avatarUrl: String?,
    val text: String,
    val createdAt: String,
    val requestKey: String? = null,
)
