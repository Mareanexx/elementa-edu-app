package ru.mareanexx.collections_api.domain.usecase

import java.text.Collator
import java.util.Locale
import ru.mareanexx.collections_api.domain.model.*

object OrderCollections {
    fun apply(items: List<BookCollection>, preferences: CollectionsPreferences, includeFavorite: Boolean): List<BookCollection> {
        val collator = Collator.getInstance(Locale.forLanguageTag("ru")).apply { strength = Collator.SECONDARY }
        val byName = Comparator<BookCollection> { a, b -> collator.compare(a.name, b.name).takeIf { it != 0 } ?: a.id.compareTo(b.id) }
        val comparator = when (preferences.sort) {
            CollectionSort.NameAscending -> byName
            CollectionSort.NameDescending -> byName.reversed()
            CollectionSort.BooksDescending -> compareByDescending<BookCollection> { it.bookCount }.then(byName)
            CollectionSort.BooksAscending -> compareBy<BookCollection> { it.bookCount }.then(byName)
        }
        val custom = items.filter { !it.isFavorite && (preferences.showEmpty || it.bookCount > 0) }.sortedWith(comparator)
        return (if (includeFavorite) items.filter { it.isFavorite } else emptyList()) + custom
    }
}
