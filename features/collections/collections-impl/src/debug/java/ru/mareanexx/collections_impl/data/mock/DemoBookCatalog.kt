package ru.mareanexx.collections_impl.data.mock

import ru.mareanexx.collections_impl.data.remote.dto.*
import ru.mareanexx.collections_impl.R

/** Данные демонстрационной библиотеки имитируют содержимое ответа сервера. */
internal object DemoBookCatalog {
    fun books(context: android.content.Context, userId: String? = null): List<CollectionBookDto> {
        val ratings = context.getSharedPreferences("book_rating_mock_cache", android.content.Context.MODE_PRIVATE)
        return items.map { book ->
            val cached = ratings.getString(book.id, null)?.let { runCatching { com.google.gson.Gson().fromJson(it, BookRatingDto::class.java) }.getOrNull() }
            val progress = context.getSharedPreferences("collections_demo_server", android.content.Context.MODE_PRIVATE)
            val key = "progress_${userId}_${book.id}"
            val read = if (userId != null && progress.contains(key)) progress.getInt(key, 0) else book.pagesRead
            book.copy(rating = cached ?: book.rating, pagesRead = read)
        }
    }

    private val titles = listOf(
        "Органическая химия", "Основы биохимии", "Квантовая химия", "Химическая термодинамика",
        "Периодический закон", "Неорганическая химия", "Лабораторный практикум", "Реакции и механизмы",
        "Метахимия", "Химические связи", "Мир полимеров", "Научные статьи о молекулах",
    )
    private val covers = listOf("chemistry", "molecules", "space", "reactions", "notes", "laboratory")
    val items = titles.mapIndexed { index, title -> CollectionBookDto(
        id = "book_${index + 1}", title = title, authors = if (index == 0) "А. И. Галочкин, И. В. Ананьина" else "Учебная редакция Elementa",
        description = "Учебный материал «$title»: основные понятия, химические закономерности и примеры для самостоятельного изучения.",
        pages = 160 + index * 24, coverKey = covers[index % covers.size],
        coverUrl = if (index == 0) "android.resource://ru.mareanexx.elementa/${R.drawable.demo_organic_book_cover}" else null,
        pagesRead = when (index) { 0 -> 48; 1 -> 92; 3 -> 232; else -> 0 },
        rating = if (index == 11) null else BookRatingDto(4.2 + (index % 7) * .1, 64 + index * 17),
        tags = listOf(
            BookTagDto(if ((index == 0 || index % 3 == 2)) "advanced" else "introductory", if ((index == 0 || index % 3 == 2)) "Продвинутый" else "Базовый", "difficulty"),
            BookTagDto("topic_$index", when (index) { 2 -> "Космос"; 1 -> "Биохимия"; 0 -> "Органическая химия"; else -> "Химия" }, "topic"),
            BookTagDto("textbook", "Учебник", "format"),
            BookTagDto("year_${2022 + index % 4}", "${2022 + index % 4}", "year"),
            BookTagDto("self_study", "Самостоятельное изучение", "format"),
        ),
    ) }
}
