package ru.mareanexx.collections_api.domain.model

data class CollectionForm(
    val name: String = "",
    val cover: CollectionCover = CollectionCover.Chemistry,
    val image: CollectionImage? = null,
)
