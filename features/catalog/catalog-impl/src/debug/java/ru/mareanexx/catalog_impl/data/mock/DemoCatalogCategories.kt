package ru.mareanexx.catalog_impl.data.mock

import ru.mareanexx.catalog_impl.data.remote.dto.CategoryDto

/** Демонстрационные данные каталога; тексты приходят клиенту по HTTP как контент. */
internal object DemoCatalogCategories {
    val membership = linkedMapOf(
        "organic" to listOf("book_1", "book_8", "book_10", "book_11"),
        "inorganic" to listOf("book_5", "book_6", "book_10"),
        "physical" to listOf("book_3", "book_4", "book_9"),
        "analytical" to listOf("book_7", "book_12"),
        "biochemistry" to listOf("book_2", "book_8"),
        "general" to listOf("book_5", "book_7", "book_9", "book_10"),
    )
    fun values(empty: Boolean = false) = listOf(
        CategoryDto("organic", "Органическая", "От строения молекул к механизмам реакций", "organic", 4),
        CategoryDto("inorganic", "Неорганическая", "Элементы, соединения и их свойства", "inorganic", 3),
        CategoryDto("physical", "Физическая", "Энергия, равновесие и квантовый мир", "physical", 3),
        CategoryDto("analytical", "Аналитическая", "Методы анализа и лабораторная практика", "analytical", 2),
        CategoryDto("biochemistry", "Биохимия", "Химия живых систем", "biochemistry", 2),
        CategoryDto("general", "Общая химия", "Основы для уверенного старта", "general", 4),
    ).map { if (empty) it.copy(bookCount = 0) else it }
}
