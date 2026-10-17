package ru.mareanexx.collections_impl.presentation.model

import ru.mareanexx.collections_api.domain.model.CollectionBook

data class RemovedCollectionBook(
    val collectionId: String,
    val book: CollectionBook,
    val index: Int,
)
