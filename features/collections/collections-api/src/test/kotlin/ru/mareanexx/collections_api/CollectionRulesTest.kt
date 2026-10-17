package ru.mareanexx.collections_api

import org.junit.Assert.*
import org.junit.Test
import ru.mareanexx.collections_api.domain.model.*
import ru.mareanexx.collections_api.domain.validation.CollectionRules
import ru.mareanexx.collections_api.domain.usecase.OrderCollections

class CollectionRulesTest {
    @Test fun `пробелы нормализуются до проверки длины`() {
        assertEquals("Органическая химия", CollectionRules.normalize("  Органическая \n химия  "))
        assertEquals(CollectionsError.NameRequired, CollectionRules.validate("  "))
        assertNull(CollectionRules.validate("я".repeat(60)))
        assertEquals(CollectionsError.NameTooLong, CollectionRules.validate("я".repeat(61)))
    }
    private val items = listOf(
        BookCollection("fav", "Избранное", CollectionCover.Chemistry, 0, true),
        BookCollection("b", "Биохимия", CollectionCover.Molecules, 5),
        BookCollection("a", "Атомы", CollectionCover.Space, 10),
        BookCollection("v", "Вещества", CollectionCover.Reactions, 0),
    )
    @Test fun `сортировка учитывает русский алфавит в обоих направлениях`() {
        assertEquals(listOf("fav", "a", "b", "v"), order(CollectionSort.NameAscending))
        assertEquals(listOf("fav", "v", "b", "a"), order(CollectionSort.NameDescending))
    }
    @Test fun `сортировка по книгам сохраняет избранное в начале`() {
        assertEquals(listOf("fav", "a", "b", "v"), order(CollectionSort.BooksDescending))
        assertEquals(listOf("fav", "v", "b", "a"), order(CollectionSort.BooksAscending))
    }
    @Test fun `пустые коллекции скрываются кроме системной`() {
        assertEquals(listOf("fav", "a", "b"), OrderCollections.apply(items, CollectionsPreferences(showEmpty = false), true).map { it.id })
        assertEquals(listOf("a", "b", "v"), OrderCollections.apply(items, CollectionsPreferences(), false).map { it.id })
    }
    private fun order(sort: CollectionSort) = OrderCollections.apply(items, CollectionsPreferences(sort), true).map { it.id }
}
