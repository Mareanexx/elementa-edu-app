package ru.mareanexx.catalog_api

import org.junit.Test
import org.junit.Assert.*
import ru.mareanexx.catalog_api.domain.validation.CatalogSearchRules

class CatalogSearchRulesTest {
    @Test fun `органическая не совпадает с неорганической по подстроке`() {
        assertFalse(CatalogSearchRules.matches("Органическая", listOf("Неорганическая химия")))
        assertTrue(CatalogSearchRules.matches("Орган", listOf("Органическая химия")))
    }
    @Test fun `слова запроса ищутся в названии авторе и темах без учёта регистра`() {
        assertTrue(CatalogSearchRules.matches("ГАЛОЧК хим", listOf("Органическая химия", "Галочкин")))
        assertFalse(CatalogSearchRules.matches("Галочкин биология", listOf("Органическая химия", "Галочкин")))
    }
    @Test fun `unicode пробелы и ё нормализуются одинаково для истории и поиска`() {
        assertEquals("ёлка химия", CatalogSearchRules.normalize("  ёлка  \t химия  "))
        assertEquals("елка химия", CatalogSearchRules.key("  ЁЛКА  химия  "))
        assertTrue(CatalogSearchRules.matches("Ёлка", listOf("Елка")))
    }
    @Test fun `запрос из одних знаков не возвращает весь каталог`() {
        assertFalse(CatalogSearchRules.matches("?!", listOf("Химия")))
        assertTrue(CatalogSearchRules.matches("", listOf("Химия")))
    }
}
