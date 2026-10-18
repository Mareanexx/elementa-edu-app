package ru.mareanexx.book_impl.data.remote.source

import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.*
import retrofit2.Response
import ru.mareanexx.book_api.domain.model.*
import ru.mareanexx.book_api.domain.validation.BookInputRules
import ru.mareanexx.book_impl.data.remote.api.BookApi
import ru.mareanexx.book_impl.data.remote.dto.*
import ru.mareanexx.book_impl.data.remote.mapper.*
import java.io.EOFException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException

internal class BookRemoteDataSource(
    private val api: BookApi,
) {
    suspend fun book(id: String) = request({ api.book(id) }, BookMapper::details)
    suspend fun favorite(id: String, selected: Boolean) = request({ if (selected) api.addFavorite(id) else api.removeFavorite(id) }, BookMapper::shelves)
    suspend fun readingList(id: String, selected: Boolean) = request({ if (selected) api.addReadingList(id) else api.removeReadingList(id) }, BookMapper::shelves)
    suspend fun collections(id: String) = request({ api.collections(id) }, BookMapper::collections)
    suspend fun saveCollections(id: String, ids: Set<String>) = request({ api.saveCollections(id, BookCollectionsRequest(ids.sorted())) }, BookMapper::shelves)
    suspend fun reviews(id: String, query: ReviewsQuery, cursor: String?) = request({ api.reviews(id, BookMapper.sort(query.sort), query.rating, cursor, 10) }, BookMapper::reviews)
    suspend fun saveReview(id: String, input: ReviewInput): BookResult<BookReview> {
        BookInputRules.review(input)?.let { return BookResult.Failure(it) }
        return request({ api.saveReview(id, BookReviewRequest(input.rating, input.text.trim())) }, BookMapper::review)
    }
    suspend fun deleteReview(id: String) = action { api.deleteReview(id) }
    suspend fun react(id: String, reviewId: String, reaction: ReviewReaction?) = request({ api.reaction(id, reviewId, BookReactionRequest(BookMapper.reaction(reaction))) }, BookMapper::review)
    suspend fun replies(id: String, reviewId: String, cursor: String?) = request({ api.replies(id, reviewId, cursor, 10) }, BookMapper::replies)
    suspend fun reply(id: String, reviewId: String, text: String, key: String): BookResult<ReviewReply> {
        BookInputRules.reply(text)?.let { return BookResult.Failure(it) }
        return request({ api.reply(id, reviewId, key, BookReplyRequest(text.trim())) }, BookMapper::reply)
    }
    suspend fun deleteReply(id: String, reviewId: String, replyId: String) = action { api.deleteReply(id, reviewId, replyId) }
    private suspend fun <T, R> request(call: suspend () -> Response<T>, map: (T) -> R): BookResult<R> = execute {
        val response = call()
        if (response.isSuccessful) BookResult.Success(map(requireNotNull(response.body()))) else BookResult.Failure(BookErrorMapper.map(response))
    }
    private suspend fun action(call: suspend () -> Response<Unit>): BookResult<Unit> = execute {
        val response = call()
        if (response.isSuccessful) BookResult.Success(Unit) else BookResult.Failure(BookErrorMapper.map(response))
    }
    private suspend fun <T> execute(block: suspend () -> BookResult<T>): BookResult<T> = withContext(Dispatchers.IO) {
        try { block()
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: SocketTimeoutException) { BookResult.Failure(BookError.Timeout)
        } catch (_: InterruptedIOException) { BookResult.Failure(BookError.Timeout)
        } catch (_: MalformedJsonException) { BookResult.Failure(BookError.InvalidResponse)
        } catch (_: EOFException) { BookResult.Failure(BookError.InvalidResponse)
        } catch (_: IOException) { BookResult.Failure(BookError.Offline)
        } catch (_: RuntimeException) { BookResult.Failure(BookError.InvalidResponse) }
    }
}
