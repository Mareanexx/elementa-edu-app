package ru.mareanexx.collections_impl.data.remote.mapper

import ru.mareanexx.collections_api.domain.model.*
import ru.mareanexx.collections_impl.data.remote.dto.*

internal object CollectionMapper {
    private val sortKeys = mapOf(
        CollectionSort.NameAscending to "name_asc",
        CollectionSort.NameDescending to "name_desc",
        CollectionSort.BooksDescending to "books_desc",
        CollectionSort.BooksAscending to "books_asc",
    )
    fun preferences(value: CollectionPreferencesDto) = CollectionsPreferences(
        sortKeys.entries.firstOrNull { it.value == value.sortOrder }?.key ?: CollectionSort.NameAscending,
        requireNotNull(value.showEmpty),
    )
    fun preferences(value: CollectionsPreferences) = CollectionPreferencesDto(sortKeys.getValue(value.sort), value.showEmpty)
    fun image(dto: CollectionImageDto) = CollectionImage(required(dto.id), required(dto.url))
    fun collection(dto: CollectionDto): BookCollection {
        require(dto.kind in listOf("favorite", "custom"))
        return BookCollection(required(dto.id), required(dto.name), cover(dto.coverKey), requireNotNull(dto.bookCount).also { require(it >= 0) }, dto.kind == "favorite", dto.image?.let(::image))
    }
    fun book(dto: CollectionBookDto) = CollectionBook(
        required(dto.id), required(dto.title), required(dto.authors), requireNotNull(dto.description),
        requireNotNull(dto.pages).also { require(it > 0) }, cover(dto.coverKey), dto.coverUrl,
        dto.rating?.let { rating ->
            val average = rating.average
            val count = rating.count
            if (average != null && average.isFinite() && average in 0.0..5.0 && count != null && count > 0) BookRating(average, count) else null
        },
        dto.tags.orEmpty().filterNotNull().mapNotNull { tag ->
            val id = tag.id?.trim()?.takeIf(String::isNotEmpty)
            val label = tag.label?.trim()?.takeIf(String::isNotEmpty)
            if (id == null || label == null) null
            else BookTag(id, label, BookTagKind.entries.firstOrNull { it.name.equals(tag.kind, true) } ?: BookTagKind.Topic)
        }.distinctBy(BookTag::id),
        (dto.pagesRead ?: 0).coerceIn(0, requireNotNull(dto.pages)),
    )
    fun cover(key: String?): CollectionCover = CollectionCover.entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: CollectionCover.Chemistry
    fun key(cover: CollectionCover) = cover.name.lowercase(java.util.Locale.ROOT)
    private fun required(value: String?): String = requireNotNull(value).also { require(it.isNotBlank()) }
}
