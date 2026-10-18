package ru.mareanexx.reader_impl.data.remote.source

import kotlinx.coroutines.*
import retrofit2.Response
import com.google.gson.stream.MalformedJsonException
import ru.mareanexx.reader_api.domain.model.*
import ru.mareanexx.reader_impl.data.remote.api.ReaderApi
import ru.mareanexx.reader_impl.data.remote.dto.ReaderProgressRequest
import ru.mareanexx.reader_impl.data.remote.mapper.ReaderMapper
import ru.mareanexx.reader_impl.data.remote.model.ReadingManifest
import java.io.*

internal class ReaderRemoteDataSource(
    private val api: ReaderApi,
    private val baseUrl: String,
    private val deviceId: String,
) {
    suspend fun manifest(id: String): ReaderResult<ReadingManifest> = request({ api.manifest(id) }) { dto -> ReaderMapper.manifest(dto, baseUrl) }
    suspend fun progress(id: String) = request({ api.progress(id) }) { ReaderMapper.position(it).also { position -> require(position.bookId == id) } }
    suspend fun savePosition(value: ReaderPosition, revision: Long) = request({ api.progress(value.bookId, ReaderProgressRequest(ReaderMapper.position(value), deviceId, revision)) }) { ReaderMapper.position(it).also { position -> require(position.bookId == value.bookId && position.version == value.version) } }
    suspend fun saveAnnotation(value: ReaderAnnotation) = request({ api.annotation(value.bookId, value.id, ReaderMapper.annotation(value)) }) { ReaderMapper.annotation(it).also { annotation -> require(annotation.bookId == value.bookId && annotation.id == value.id) } }
    suspend fun annotations(id: String, cursor: String?) = request({ api.annotations(id, cursor) }) { dto -> requireNotNull(dto.items).map(ReaderMapper::annotation) to dto.nextCursor?.takeIf(String::isNotBlank) }
    suspend fun delete(value: ReaderAnnotation): ReaderResult<Unit> = withContext(Dispatchers.IO) {
        try { val response = api.delete(value.bookId, value.id); if (response.isSuccessful || response.code() == 404) ReaderResult.Success(Unit) else ReaderResult.Failure(httpError(response.code()))
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: IOException) { ReaderResult.Failure(ReaderError.Offline) }
    }
    private suspend fun <T, R> request(call: suspend () -> Response<T>, map: (T) -> R): ReaderResult<R> = withContext(Dispatchers.IO) {
        try {
            val response = call()
            if (response.isSuccessful) ReaderResult.Success(map(requireNotNull(response.body()))) else ReaderResult.Failure(httpError(response.code()))
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: InterruptedIOException) { ReaderResult.Failure(ReaderError.Timeout)
        } catch (_: MalformedJsonException) { ReaderResult.Failure(ReaderError.InvalidResponse)
        } catch (_: EOFException) { ReaderResult.Failure(ReaderError.InvalidResponse)
        } catch (_: IOException) { ReaderResult.Failure(ReaderError.Offline)
        } catch (_: RuntimeException) { ReaderResult.Failure(ReaderError.InvalidResponse) }
    }
    companion object {
        fun httpError(code: Int) = when (code) {
            401 -> ReaderError.Unauthorized
            403 -> ReaderError.Forbidden
            404 -> ReaderError.NotFound
            429 -> ReaderError.RateLimited
            in 500..599 -> ReaderError.Unavailable
            else -> ReaderError.InvalidResponse
        }
    }
}
