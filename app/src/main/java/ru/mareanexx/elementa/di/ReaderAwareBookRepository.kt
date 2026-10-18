package ru.mareanexx.elementa.di

import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_api.domain.repository.BookRepository
import ru.mareanexx.reader_api.domain.repository.ReaderRepository

internal class ReaderAwareBookRepository(
    private val books: BookRepository,
    private val reader: ReaderRepository,
) : BookRepository by books {
    override suspend fun getBook(id: String) = books.getBook(id).also { result ->
        if (result is BookResult.Success) reader.updateLibraryState(id, result.value.shelves)
    }
    override suspend fun setFavorite(id: String, selected: Boolean) = shelves(id) { books.setFavorite(id, selected) }
    override suspend fun setWantToRead(id: String, selected: Boolean) = shelves(id) { books.setWantToRead(id, selected) }
    override suspend fun saveCollections(id: String, ids: Set<String>) = shelves(id) { books.saveCollections(id, ids) }
    private suspend fun shelves(id: String, action: suspend () -> BookResult<BookShelves>): BookResult<BookShelves> = action().also { result ->
        if (result is BookResult.Success) reader.updateLibraryState(id, result.value)
    }
}
