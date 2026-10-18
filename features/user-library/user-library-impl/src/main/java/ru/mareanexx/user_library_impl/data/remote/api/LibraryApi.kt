package ru.mareanexx.user_library_impl.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import ru.mareanexx.user_library_impl.data.remote.dto.*

internal interface LibraryApi {
    @GET("api/v1/users/me/library")
    suspend fun books(
        @Query("status") status: String,
        @Query("query") query: String?,
        @Query("sort") sort: String,
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int,
    ): Response<LibraryPageDto>
    @GET("api/v1/users/me/library/preferences")
    suspend fun preferences(): Response<LibraryPreferencesDto>
    @PATCH("api/v1/users/me/library/preferences")
    suspend fun savePreferences(@Body body: LibraryPreferencesDto): Response<LibraryPreferencesDto>
    @PUT("api/v1/users/me/library/books/{bookId}/reading-status")
    suspend fun readingStatus(@Path("bookId") id: String, @Body body: LibraryStatusRequest): Response<LibraryBookDto>
}
