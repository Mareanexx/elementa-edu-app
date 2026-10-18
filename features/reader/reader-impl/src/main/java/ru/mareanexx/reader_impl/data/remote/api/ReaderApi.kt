package ru.mareanexx.reader_impl.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import ru.mareanexx.reader_impl.data.remote.dto.*

internal interface ReaderApi {
    @GET("api/v1/books/{id}/reading-manifest")
    suspend fun manifest(@Path("id") id: String): Response<ReadingManifestDto>
    @GET("api/v1/users/me/reading/books/{id}/progress")
    suspend fun progress(@Path("id") id: String): Response<ReaderPositionDto>
    @PUT("api/v1/users/me/reading/books/{id}/progress")
    suspend fun progress(@Path("id") id: String, @Body value: ReaderProgressRequest): Response<ReaderPositionDto>
    @GET("api/v1/users/me/reading/books/{id}/annotations")
    suspend fun annotations(@Path("id") id: String, @Query("cursor") cursor: String?, @Query("limit") limit: Int = 100): Response<ReaderAnnotationsPageDto>
    @PUT("api/v1/users/me/reading/books/{id}/annotations/{annotationId}")
    suspend fun annotation(@Path("id") id: String, @Path("annotationId") annotationId: String, @Body value: ReaderAnnotationDto): Response<ReaderAnnotationDto>
    @DELETE("api/v1/users/me/reading/books/{id}/annotations/{annotationId}")
    suspend fun delete(@Path("id") id: String, @Path("annotationId") annotationId: String): Response<Unit>
}
