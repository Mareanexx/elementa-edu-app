package ru.mareanexx.collections_impl.data.mock

internal data class DemoStoredCollection(
    val id: String,
    val name: String,
    val coverKey: String,
    val favorite: Boolean,
    val bookIds: List<String>,
    val coverImageId: String? = null,
    val readingList: Boolean = false,
)
