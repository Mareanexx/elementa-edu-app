package ru.mareanexx.book_impl.data.remote.mapper

import com.google.gson.JsonParser
import retrofit2.Response
import ru.mareanexx.book_api.domain.model.BookError

internal object BookErrorMapper {
    fun map(response: Response<*>): BookError {
        val code = runCatching { JsonParser.parseString(response.errorBody()?.string()).asJsonObject.getAsJsonObject("error").get("code").asString }.getOrNull()
        return when (code) {
            "RATING_REQUIRED" -> BookError.RatingRequired
            "REVIEW_TOO_LONG" -> BookError.ReviewTooLong
            "REPLY_REQUIRED" -> BookError.ReplyRequired
            "REPLY_TOO_LONG" -> BookError.ReplyTooLong
            "COLLECTION_NOT_FOUND" -> BookError.CollectionNotFound
            else -> when (response.code()) {
                401 -> BookError.Unauthorized
                403 -> BookError.Forbidden
                404 -> BookError.NotFound
                409 -> BookError.Conflict
                429 -> BookError.RateLimited
                in 500..599 -> BookError.Unavailable
                else -> BookError.InvalidResponse
            }
        }
    }
}
