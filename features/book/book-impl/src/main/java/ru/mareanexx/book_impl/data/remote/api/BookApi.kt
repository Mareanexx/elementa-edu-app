package ru.mareanexx.book_impl.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import ru.mareanexx.book_impl.data.remote.dto.*

internal interface BookApi {
    @GET("api/v1/books/{id}")
    suspend fun book(@Path("id") id: String): Response<BookDetailsDto>
    @PUT("api/v1/users/me/books/{id}/favorite")
    suspend fun addFavorite(@Path("id") id: String): Response<BookShelvesDto>
    @DELETE("api/v1/users/me/books/{id}/favorite")
    suspend fun removeFavorite(@Path("id") id: String): Response<BookShelvesDto>
    @PUT("api/v1/users/me/books/{id}/reading-list")
    suspend fun addReadingList(@Path("id") id: String): Response<BookShelvesDto>
    @DELETE("api/v1/users/me/books/{id}/reading-list")
    suspend fun removeReadingList(@Path("id") id: String): Response<BookShelvesDto>
    @GET("api/v1/users/me/books/{id}/collections")
    suspend fun collections(@Path("id") id: String): Response<BookCollectionsDto>
    @PUT("api/v1/users/me/books/{id}/collections")
    suspend fun saveCollections(@Path("id") id: String, @Body body: BookCollectionsRequest): Response<BookShelvesDto>
    @GET("api/v1/books/{id}/reviews")
    suspend fun reviews(@Path("id") id: String, @Query("sort") sort: String, @Query("rating") rating: Int?, @Query("cursor") cursor: String?, @Query("limit") limit: Int): Response<BookReviewsPageDto>
    @PUT("api/v1/books/{id}/reviews/me")
    suspend fun saveReview(@Path("id") id: String, @Body body: BookReviewRequest): Response<BookReviewDto>
    @DELETE("api/v1/books/{id}/reviews/me")
    suspend fun deleteReview(@Path("id") id: String): Response<Unit>
    @PUT("api/v1/books/{id}/reviews/{review}/reaction")
    suspend fun reaction(@Path("id") id: String, @Path("review") reviewId: String, @Body body: BookReactionRequest): Response<BookReviewDto>
    @GET("api/v1/books/{id}/reviews/{review}/replies")
    suspend fun replies(@Path("id") id: String, @Path("review") reviewId: String, @Query("cursor") cursor: String?, @Query("limit") limit: Int): Response<BookRepliesPageDto>
    @POST("api/v1/books/{id}/reviews/{review}/replies")
    suspend fun reply(@Path("id") id: String, @Path("review") reviewId: String, @Header("Idempotency-Key") key: String, @Body body: BookReplyRequest): Response<BookReplyDto>
    @DELETE("api/v1/books/{id}/reviews/{review}/replies/{reply}")
    suspend fun deleteReply(@Path("id") id: String, @Path("review") reviewId: String, @Path("reply") replyId: String): Response<Unit>
}
