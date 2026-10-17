package ru.mareanexx.collections_impl.data.remote.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.mareanexx.collections_impl.data.remote.dto.*

internal interface CollectionsApi {
    @Multipart
    @POST("api/v1/users/me/collection-covers")
    suspend fun uploadCover(@Part image: MultipartBody.Part): Response<CollectionImageDto>
    @GET("api/v1/users/me/collection-preferences")
    suspend fun getPreferences(): Response<CollectionPreferencesDto>
    @PATCH("api/v1/users/me/collection-preferences")
    suspend fun setPreferences(@Body value: CollectionPreferencesDto): Response<CollectionPreferencesDto>
    @GET("api/v1/users/me/collections")
    suspend fun getCollections(): Response<CollectionsResponse>
    @POST("api/v1/users/me/collections")
    suspend fun create(@Body form: SaveCollectionRequest): Response<CollectionDto>
    @PATCH("api/v1/users/me/collections/{id}")
    suspend fun update(@Path("id") id: String, @Body form: SaveCollectionRequest): Response<CollectionDto>
    @DELETE("api/v1/users/me/collections/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
    @GET("api/v1/users/me/collections/{id}/books")
    suspend fun getBooks(@Path("id") id: String): Response<CollectionBooksResponse>
    @POST("api/v1/users/me/collections/{id}/books")
    suspend fun addBooks(@Path("id") id: String, @Body request: CollectionBooksRequest): Response<Unit>
    @DELETE("api/v1/users/me/collections/{id}/books/{bookId}")
    suspend fun removeBook(@Path("id") id: String, @Path("bookId") bookId: String): Response<Unit>
    @GET("api/v1/users/me/library/books")
    suspend fun getLibrary(): Response<CollectionBooksResponse>
}
