package ru.mareanexx.collections_api.domain.model

data class CollectionsPreferences(
    val sort: CollectionSort = CollectionSort.NameAscending,
    val showEmpty: Boolean = true,
)
