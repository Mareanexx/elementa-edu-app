package ru.mareanexx.user_library_impl.data.remote.source

import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.*
import retrofit2.Response
import ru.mareanexx.user_library_api.domain.model.*
import ru.mareanexx.user_library_api.domain.validation.LibrarySearchRules
import ru.mareanexx.user_library_impl.data.remote.api.LibraryApi
import ru.mareanexx.user_library_impl.data.remote.dto.*
import ru.mareanexx.user_library_impl.data.remote.mapper.LibraryMapper
import java.io.*

internal class LibraryRemoteDataSource(
    private val api: LibraryApi,
) {
    suspend fun books(query: LibraryQuery, cursor: String?): LibraryResult<LibraryPage> {
        val normalized = LibrarySearchRules.normalize(query.search)
        if (!LibrarySearchRules.canSearch(normalized)) return LibraryResult.Failure(LibraryError.InvalidResponse)
        return request({ api.books(LibraryMapper.category(query.category), normalized.takeIf(String::isNotEmpty), LibraryMapper.sort(query.sort), cursor, 20) }, LibraryMapper::page)
    }
    suspend fun preferences() = request({ api.preferences() }, LibraryMapper::preferences)
    suspend fun savePreferences(value: LibraryPreferences) = request({ api.savePreferences(LibraryPreferencesDto(LibraryMapper.sort(value.sort))) }, LibraryMapper::preferences)
    suspend fun status(id: String, status: ReadingStatus) = request({ api.readingStatus(id, LibraryStatusRequest(LibraryMapper.status(status))) }, LibraryMapper::book)
    private suspend fun <T, R> request(call: suspend () -> Response<T>, map: (T) -> R): LibraryResult<R> = withContext(Dispatchers.IO) {
        try {
            val response = call()
            if (response.isSuccessful) LibraryResult.Success(map(requireNotNull(response.body())))
            else LibraryResult.Failure(when (response.code()) {
                401 -> LibraryError.Unauthorized
                404 -> LibraryError.NotFound
                409 -> LibraryError.Conflict
                429 -> LibraryError.RateLimited
                in 500..599 -> LibraryError.Unavailable
                else -> LibraryError.InvalidResponse
            })
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: InterruptedIOException) { LibraryResult.Failure(LibraryError.Timeout)
        } catch (_: MalformedJsonException) { LibraryResult.Failure(LibraryError.InvalidResponse)
        } catch (_: EOFException) { LibraryResult.Failure(LibraryError.InvalidResponse)
        } catch (_: IOException) { LibraryResult.Failure(LibraryError.Offline)
        } catch (_: RuntimeException) { LibraryResult.Failure(LibraryError.InvalidResponse) }
    }
}
