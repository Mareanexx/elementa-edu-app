package ru.mareanexx.user_library_impl

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_api.domain.repository.BookRepository

internal class FakeShelfRepository : BookRepository {
    override val libraryChanges = MutableSharedFlow<Unit>()
    var favoriteCalls = 0
    var gate: CompletableDeferred<Unit>? = null
    var failure: BookError? = null
    override suspend fun setFavorite(id: String, selected: Boolean): BookResult<BookShelves> {
        favoriteCalls++
        gate?.await()
        return failure?.let { BookResult.Failure(it) } ?: BookResult.Success(BookShelves(selected, false, 0))
    }
    override suspend fun setWantToRead(id: String, selected: Boolean) = BookResult.Success(BookShelves(false, selected, 0))
    override suspend fun getBook(id: String): BookResult<BookDetails> = error("Не используется в сценарии")
    override suspend fun getCollections(id: String): BookResult<List<BookCollectionChoice>> = error("Не используется в сценарии")
    override suspend fun saveCollections(id: String, ids: Set<String>): BookResult<BookShelves> = error("Не используется в сценарии")
    override suspend fun getReviews(id: String, query: ReviewsQuery, cursor: String?): BookResult<ReviewsPage> = error("Не используется в сценарии")
    override suspend fun saveReview(id: String, input: ReviewInput): BookResult<BookReview> = error("Не используется в сценарии")
    override suspend fun deleteReview(id: String): BookResult<Unit> = error("Не используется в сценарии")
    override suspend fun react(id: String, reviewId: String, reaction: ReviewReaction?): BookResult<BookReview> = error("Не используется в сценарии")
    override suspend fun getReplies(id: String, reviewId: String, cursor: String?): BookResult<RepliesPage> = error("Не используется в сценарии")
    override suspend fun reply(id: String, reviewId: String, text: String, idempotencyKey: String): BookResult<ReviewReply> = error("Не используется в сценарии")
    override suspend fun deleteReply(id: String, reviewId: String, replyId: String): BookResult<Unit> = error("Не используется в сценарии")
}
