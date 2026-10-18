package ru.mareanexx.book_impl.data.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_api.domain.repository.BookRepository
import ru.mareanexx.book_impl.data.remote.source.BookRemoteDataSource

internal class DefaultBookRepository(
    private val remote: BookRemoteDataSource,
) : BookRepository {
    private val changes = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    override val libraryChanges = changes.asSharedFlow()
    override suspend fun getBook(id: String) = remote.book(id)
    override suspend fun setFavorite(id: String, selected: Boolean) = changed { remote.favorite(id, selected) }
    override suspend fun setWantToRead(id: String, selected: Boolean) = changed { remote.readingList(id, selected) }
    override suspend fun getCollections(id: String) = remote.collections(id)
    override suspend fun saveCollections(id: String, ids: Set<String>) = changed { remote.saveCollections(id, ids) }
    override suspend fun getReviews(id: String, query: ReviewsQuery, cursor: String?) = remote.reviews(id, query, cursor)
    override suspend fun saveReview(id: String, input: ReviewInput) = changed { remote.saveReview(id, input) }
    override suspend fun deleteReview(id: String) = changed { remote.deleteReview(id) }
    override suspend fun react(id: String, reviewId: String, reaction: ReviewReaction?) = remote.react(id, reviewId, reaction)
    override suspend fun getReplies(id: String, reviewId: String, cursor: String?) = remote.replies(id, reviewId, cursor)
    override suspend fun reply(id: String, reviewId: String, text: String, idempotencyKey: String) = remote.reply(id, reviewId, text, idempotencyKey)
    override suspend fun deleteReply(id: String, reviewId: String, replyId: String) = remote.deleteReply(id, reviewId, replyId)
    private suspend fun <T> changed(block: suspend () -> BookResult<T>): BookResult<T> = block().also { if (it is BookResult.Success) changes.emit(Unit) }
}
