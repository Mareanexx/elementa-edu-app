package ru.mareanexx.collections_impl.data.remote.mapper

import com.google.gson.JsonParser
import retrofit2.Response
import ru.mareanexx.collections_api.domain.model.CollectionsError

internal object CollectionsErrorMapper {
    fun map(response: Response<*>): CollectionsError {
        if (response.code() == 401) return CollectionsError.Unauthorized
        val code = runCatching { JsonParser.parseString(response.errorBody()?.string()).asJsonObject.getAsJsonObject("error").get("code").asString }.getOrNull()
        return when (code) {
            "INVALID_COVER_IMAGE" -> CollectionsError.InvalidImage
            "NAME_REQUIRED" -> CollectionsError.NameRequired
            "NAME_TOO_LONG" -> CollectionsError.NameTooLong
            "NAME_TAKEN" -> CollectionsError.NameTaken
            "COLLECTION_LIMIT_REACHED" -> CollectionsError.LimitReached
            "COLLECTION_PROTECTED" -> CollectionsError.Protected
            "INVALID_BOOKS" -> CollectionsError.InvalidBooks
            else -> when (response.code()) {
                404 -> CollectionsError.NotFound
                408, 504 -> CollectionsError.Timeout
                413 -> CollectionsError.InvalidImage
                429 -> CollectionsError.RateLimited
                else -> CollectionsError.Unavailable
            }
        }
    }
}
