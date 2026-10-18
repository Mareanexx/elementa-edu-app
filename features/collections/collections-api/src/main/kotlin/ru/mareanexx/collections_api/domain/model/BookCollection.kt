package ru.mareanexx.collections_api.domain.model

data class BookCollection(
    val id: String,
    val name: String,
    val cover: CollectionCover,
    val bookCount: Int,
    val isFavorite: Boolean = false,
    val image: CollectionImage? = null,
    val isReadingList: Boolean = false,
) {
    val isSystem: Boolean get() = isFavorite || isReadingList
}
