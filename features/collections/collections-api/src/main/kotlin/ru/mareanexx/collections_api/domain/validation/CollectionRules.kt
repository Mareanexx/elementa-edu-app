package ru.mareanexx.collections_api.domain.validation

import ru.mareanexx.collections_api.domain.model.CollectionsError

object CollectionRules {
    const val MAX_COLLECTIONS = 30
    const val MAX_NAME_LENGTH = 60
    fun normalize(value: String): String = value.trim().replace(Regex("\\s+"), " ")
    fun validate(value: String): CollectionsError? = when {
        normalize(value).isEmpty() -> CollectionsError.NameRequired
        normalize(value).length > MAX_NAME_LENGTH -> CollectionsError.NameTooLong
        else -> null
    }
}
