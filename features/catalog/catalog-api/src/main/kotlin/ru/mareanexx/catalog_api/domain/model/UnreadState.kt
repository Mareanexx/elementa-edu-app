package ru.mareanexx.catalog_api.domain.model

/** Ревизия защищает счётчик от запоздавших ответов параллельных запросов. */
data class UnreadState(
    val count: Int,
    val revision: Long,
)
