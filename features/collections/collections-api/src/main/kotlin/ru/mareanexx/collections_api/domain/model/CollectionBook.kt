package ru.mareanexx.collections_api.domain.model

data class CollectionBook(
    val id: String,
    val title: String,
    val authors: String,
    val description: String,
    val pages: Int,
    val cover: CollectionCover,
    val coverUrl: String? = null,
    val rating: BookRating? = null,
    val tags: List<BookTag> = emptyList(),
    val pagesRead: Int = 0,
)
