package ru.mareanexx.book_impl.data.mock

internal data class DemoStoredReview(
    val id: String,
    val authorId: String,
    val authorName: String,
    val avatarUrl: String?,
    val rating: Int,
    val text: String,
    val createdAt: String,
    val updatedAt: String?,
    val baseLikes: Int,
    val baseDislikes: Int,
    val reactions: Map<String, String> = emptyMap(),
)
