package ru.mareanexx.catalog_api.domain.validation

import java.text.Normalizer
import java.util.Locale

object CatalogSearchRules {
    const val MIN_LENGTH = 2
    const val MAX_LENGTH = 100
    const val HISTORY_SIZE = 8
    fun normalize(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFKC).trim().replace(Regex("\\s+"), " ")
    fun valid(value: String) = value.length in MIN_LENGTH..MAX_LENGTH
    fun key(value: String) = normalize(value).lowercase(Locale.ROOT).replace('ё', 'е')
    fun tokens(value: String) = key(value).split(Regex("[^\\p{L}\\p{N}]+")).filter(String::isNotBlank)
    fun matches(query: String, fields: List<String>): Boolean {
        if (normalize(query).isEmpty()) return true
        val terms = tokens(query)
        if (terms.isEmpty()) return false
        val words = fields.flatMap(::tokens)
        return terms.all { term -> words.any { it.startsWith(term) } }
    }
}
