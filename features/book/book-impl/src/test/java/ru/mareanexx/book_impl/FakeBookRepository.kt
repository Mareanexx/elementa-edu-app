package ru.mareanexx.book_impl

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_api.domain.repository.BookRepository

internal class FakeBookRepository : BookRepository {
    override val libraryChanges = MutableSharedFlow<Unit>()
    var book = BookFixtures.book()
    var failure: BookError? = null
    var bookGate: CompletableDeferred<Unit>? = null
    var favoriteGate: CompletableDeferred<Unit>? = null
    var reviewGate: CompletableDeferred<Unit>? = null
    var favoriteCalls = 0
    var saveCalls = 0
    var reactionCalls = 0
    var reviewGates = mutableMapOf<ReviewSort, CompletableDeferred<Unit>>()
    var pages = mutableMapOf(ReviewSort.Helpful to listOf(BookFixtures.review()))
    var replyKeys = mutableListOf<String>()
    var lostReplyResponse = false
    val storedReplies = mutableMapOf<String, ReviewReply>()
    var choices = listOf(BookCollectionChoice("c", "Химия", 0, false))
    private fun <T> result(value: T): BookResult<T> = failure?.let { BookResult.Failure(it) } ?: BookResult.Success(value)
    override suspend fun getBook(id: String): BookResult<BookDetails> { val snapshot = result(book); bookGate?.await(); return snapshot }
    override suspend fun setFavorite(id: String, selected: Boolean): BookResult<BookShelves> {
        favoriteCalls++; favoriteGate?.await()
        if (failure == null) book = book.copy(shelves = book.shelves.copy(favorite = selected))
        return result(book.shelves)
    }
    override suspend fun setWantToRead(id: String, selected: Boolean): BookResult<BookShelves> { if (failure == null) book = book.copy(shelves = book.shelves.copy(wantToRead = selected)); return result(book.shelves) }
    override suspend fun getCollections(id: String) = result(choices)
    override suspend fun saveCollections(id: String, ids: Set<String>): BookResult<BookShelves> { if (failure == null) book = book.copy(shelves = book.shelves.copy(collectionCount = ids.size)); return result(book.shelves) }
    override suspend fun getReviews(id: String, query: ReviewsQuery, cursor: String?): BookResult<ReviewsPage> {
        val snapshot = pages[query.sort].orEmpty()
        reviewGates[query.sort]?.await()
        return result(ReviewsPage(snapshot, null, snapshot.size))
    }
    override suspend fun saveReview(id: String, input: ReviewInput): BookResult<BookReview> {
        saveCalls++; reviewGate?.await()
        val review = BookFixtures.review(mine = true).copy(rating = input.rating, text = input.text)
        if (failure == null) book = book.copy(myReview = review)
        return result(review)
    }
    override suspend fun deleteReview(id: String): BookResult<Unit> { if (failure == null) book = book.copy(myReview = null); return result(Unit) }
    override suspend fun react(id: String, reviewId: String, reaction: ReviewReaction?): BookResult<BookReview> {
        reactionCalls++
        val review = pages.getValue(ReviewSort.Helpful).first { it.id == reviewId }.copy(reaction = reaction)
        if (failure == null) pages[ReviewSort.Helpful] = listOf(review)
        return result(review)
    }
    override suspend fun getReplies(id: String, reviewId: String, cursor: String?) = result(RepliesPage(storedReplies.values.toList(), null, storedReplies.size))
    override suspend fun reply(id: String, reviewId: String, text: String, idempotencyKey: String): BookResult<ReviewReply> {
        replyKeys += idempotencyKey
        val item = storedReplies.getOrPut(idempotencyKey) { ReviewReply("reply-${storedReplies.size}", "Мария", null, text, 1_750_000_000_000, true) }
        if (lostReplyResponse) { lostReplyResponse = false; return BookResult.Failure(BookError.Offline) }
        return result(item)
    }
    override suspend fun deleteReply(id: String, reviewId: String, replyId: String): BookResult<Unit> { storedReplies.entries.removeAll { it.value.id == replyId }; return result(Unit) }
}
