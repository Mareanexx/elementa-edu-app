package ru.mareanexx.catalog_impl.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import ru.mareanexx.catalog_impl.data.remote.dto.*

internal interface CatalogApi {
    @GET("api/v1/users/me/home")
    @Headers("Accept-Language: ru")
    suspend fun home(): Response<HomeFeedDto>
    @GET("api/v1/catalog/categories")
    @Headers("Accept-Language: ru")
    suspend fun categories(): Response<List<CategoryDto>>
    @GET("api/v1/catalog/books")
    @Headers("Accept-Language: ru")
    suspend fun books(@Query("category_id") category: String?, @Query("query") query: String?, @Query("sort") sort: String, @Query("cursor") cursor: String?, @Query("limit") limit: Int): Response<CatalogPageDto>
    @GET("api/v1/catalog/search/suggestions")
    @Headers("Accept-Language: ru")
    suspend fun suggestions(): Response<SuggestionsDto>
    @GET("api/v1/users/me/notifications")
    @Headers("Accept-Language: ru")
    suspend fun notifications(@Query("filter") filter: String, @Query("cursor") cursor: String?, @Query("limit") limit: Int): Response<NotificationPageDto>
    @PUT("api/v1/users/me/notifications/{id}/read")
    suspend fun markRead(@Path("id") id: String): Response<NotificationReceiptDto>
    @PUT("api/v1/users/me/notifications/read-state")
    suspend fun markAllRead(@Body request: ReadStateRequest): Response<NotificationReceiptDto>
}
