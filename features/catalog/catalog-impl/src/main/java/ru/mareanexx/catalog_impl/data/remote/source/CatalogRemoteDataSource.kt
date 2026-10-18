package ru.mareanexx.catalog_impl.data.remote.source

import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.*
import retrofit2.Response
import ru.mareanexx.catalog_api.domain.model.*
import ru.mareanexx.catalog_api.domain.validation.CatalogSearchRules
import ru.mareanexx.catalog_impl.data.remote.api.CatalogApi
import ru.mareanexx.catalog_impl.data.remote.dto.ReadStateRequest
import ru.mareanexx.catalog_impl.data.remote.mapper.CatalogMapper
import java.io.*

internal class CatalogRemoteDataSource(
    private val api: CatalogApi,
) {
    suspend fun home() = request(api::home, CatalogMapper::home)
    suspend fun categories() = request(api::categories) { values -> values.map(CatalogMapper::category).distinctBy { it.id } }
    suspend fun books(query: CatalogQuery, cursor: String?): CatalogResult<CatalogPage> {
        val normalized = CatalogSearchRules.normalize(query.search)
        if (normalized.isNotEmpty() && !CatalogSearchRules.valid(normalized)) return CatalogResult.Failure(CatalogError.InvalidResponse)
        return request({ api.books(query.categoryId, normalized.takeIf(String::isNotEmpty), CatalogMapper.sort(query.sort), cursor, 20) }, CatalogMapper::page)
    }
    suspend fun suggestions() = request(api::suggestions) { it.queries.orEmpty().map(CatalogSearchRules::normalize).filter(CatalogSearchRules::valid).distinct().take(8) }
    suspend fun notifications(filter: NotificationFilter, cursor: String?) = request({ api.notifications(if (filter == NotificationFilter.All) "all" else "unread", cursor, 20) }, CatalogMapper::notifications)
    suspend fun markRead(id: String) = request({ api.markRead(id) }, CatalogMapper::receipt)
    suspend fun markAllRead(sequence: Long) = request({ api.markAllRead(ReadStateRequest(sequence)) }, CatalogMapper::receipt)
    private suspend fun <T, R> request(call: suspend () -> Response<T>, map: (T) -> R): CatalogResult<R> = withContext(Dispatchers.IO) {
        try {
            val response = call()
            if (response.isSuccessful) CatalogResult.Success(map(requireNotNull(response.body())))
            else CatalogResult.Failure(when (response.code()) {
                401 -> CatalogError.Unauthorized
                404 -> CatalogError.NotFound
                409 -> CatalogError.Conflict
                429 -> CatalogError.RateLimited
                in 500..599 -> CatalogError.Unavailable
                else -> CatalogError.InvalidResponse
            })
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: InterruptedIOException) { CatalogResult.Failure(CatalogError.Timeout)
        } catch (_: MalformedJsonException) { CatalogResult.Failure(CatalogError.InvalidResponse)
        } catch (_: EOFException) { CatalogResult.Failure(CatalogError.InvalidResponse)
        } catch (_: IOException) { CatalogResult.Failure(CatalogError.Offline)
        } catch (_: RuntimeException) { CatalogResult.Failure(CatalogError.InvalidResponse) }
    }
}
