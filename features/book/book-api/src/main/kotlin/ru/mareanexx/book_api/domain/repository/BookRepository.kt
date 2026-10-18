package ru.mareanexx.book_api.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mareanexx.book_api.domain.model.*

interface BookRepository {
    val libraryChanges: Flow<Unit>
    suspend fun getBook(id: String): BookResult<BookDetails>
    suspend fun setFavorite(id: String, selected: Boolean): BookResult<BookShelves>
    suspend fun setWantToRead(id: String, selected: Boolean): BookResult<BookShelves>
    suspend fun getCollections(id: String): BookResult<List<BookCollectionChoice>>
    suspend fun saveCollections(id: String, ids: Set<String>): BookResult<BookShelves>
    suspend fun getReviews(id: String, query: ReviewsQuery, cursor: String?): BookResult<ReviewsPage>
    suspend fun saveReview(id: String, input: ReviewInput): BookResult<BookReview>
    suspend fun deleteReview(id: String): BookResult<Unit>
    suspend fun react(id: String, reviewId: String, reaction: ReviewReaction?): BookResult<BookReview>
    suspend fun getReplies(id: String, reviewId: String, cursor: String?): BookResult<RepliesPage>
    suspend fun reply(id: String, reviewId: String, text: String, idempotencyKey: String): BookResult<ReviewReply>
    suspend fun deleteReply(id: String, reviewId: String, replyId: String): BookResult<Unit>
}
