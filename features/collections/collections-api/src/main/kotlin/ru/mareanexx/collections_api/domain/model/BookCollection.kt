package ru.mareanexx.collections_api.domain.model

data class BookCollection(
    val id: String,
    val name: String,
    val cover: CollectionCover,
    val bookCount: Int,
    val isFavorite: Boolean = false,
    val image: CollectionImage? = null,
)
