package ru.mareanexx.collections_impl.presentation.model

import ru.mareanexx.collections_api.domain.model.*

data class CollectionPickerState(
    val books: List<CollectionBook> = emptyList(),
    val selected: Set<String> = emptySet(),
    val existing: Set<String> = emptySet(),
    val query: String = "",
    val loading: Boolean = true,
    val error: CollectionsError? = null,
)
