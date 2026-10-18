package ru.mareanexx.user_library_api.domain.validation

object LibrarySearchRules {
    const val MIN_LENGTH = 2
    const val MAX_LENGTH = 100
    const val HISTORY_LIMIT = 8
    fun normalize(value: String): String = value.trim().replace(Regex("\\s+"), " ").take(MAX_LENGTH)
    fun canSearch(value: String): Boolean = value.isEmpty() || value.length >= MIN_LENGTH
}
