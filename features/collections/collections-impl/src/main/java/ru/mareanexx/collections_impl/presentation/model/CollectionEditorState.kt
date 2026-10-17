package ru.mareanexx.collections_impl.presentation.model

import ru.mareanexx.collections_api.domain.model.*

data class CollectionEditorState(
    val id: String? = null,
    val form: CollectionForm = CollectionForm(),
    val fieldError: CollectionsError? = null,
    val coverBusy: Boolean = false,
    val requestError: CollectionsError? = null,
)
