package ru.mareanexx.collections_impl.data.mock

import android.content.Context
import com.google.gson.Gson
import ru.mareanexx.collections_api.domain.model.BookCollection
import ru.mareanexx.collections_api.domain.model.CollectionBook
import ru.mareanexx.collections_impl.data.remote.dto.BookRatingDto
import ru.mareanexx.collections_impl.data.remote.mapper.CollectionMapper

/** Только для debug: оба HTTP-мока используют одну библиотеку и одни системные подборки. */
class DemoBookLibraryBridge(
    private val context: Context,
    private val userId: String,
) {
    private val store = DemoCollectionsStore(context, userId)
    fun book(id: String): CollectionBook? = DemoBookCatalog.books(context, userId).firstOrNull { it.id == id }?.let(CollectionMapper::book)
    fun books(): List<CollectionBook> = DemoBookCatalog.books(context, userId).map(CollectionMapper::book)
    fun setPagesRead(bookId: String, read: Int) = synchronized(DemoCollectionsStore.lock) {
        val book = requireNotNull(book(bookId))
        require(read in 0..book.pages)
        if (!context.getSharedPreferences("collections_demo_server", Context.MODE_PRIVATE).edit()
                .putInt("progress_${userId}_$bookId", read).commit()) throw java.io.IOException("Не удалось сохранить прогресс чтения")
    }
    fun collections(): List<BookCollection> = synchronized(DemoCollectionsStore.lock) {
        store.read().map { item -> BookCollection(item.id, item.name, CollectionMapper.cover(item.coverKey), item.bookIds.size, item.favorite, isReadingList = item.readingList) }
    }
    fun contains(collectionId: String, bookId: String): Boolean = synchronized(DemoCollectionsStore.lock) {
        store.read().firstOrNull { it.id == collectionId }?.bookIds?.contains(bookId) == true
    }
    fun setSystem(bookId: String, readingList: Boolean, selected: Boolean) = synchronized(DemoCollectionsStore.lock) {
        require(book(bookId) != null)
        val records = store.read()
        val target = records.first { if (readingList) it.readingList else it.favorite }
        store.save(records.map { item -> if (item.id != target.id) item else item.copy(bookIds = if (selected) (item.bookIds + bookId).distinct() else item.bookIds - bookId) })
    }
    fun setCustom(bookId: String, ids: Set<String>) = synchronized(DemoCollectionsStore.lock) {
        val records = store.read()
        require(book(bookId) != null && ids.all { id -> records.any { it.id == id && !it.favorite && !it.readingList } })
        store.save(records.map { item ->
            if (item.favorite || item.readingList) item else item.copy(bookIds = if (item.id in ids) (item.bookIds + bookId).distinct() else item.bookIds - bookId)
        })
    }
    fun cacheRating(bookId: String, average: Double?, count: Int) {
        check(context.getSharedPreferences("book_rating_mock_cache", Context.MODE_PRIVATE).edit()
            .putString(bookId, Gson().toJson(BookRatingDto(average ?: 0.0, count))).commit())
    }
}
