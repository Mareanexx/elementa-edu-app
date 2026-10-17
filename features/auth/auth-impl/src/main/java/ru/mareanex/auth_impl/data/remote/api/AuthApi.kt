package ru.mareanex.auth_impl.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import ru.mareanex.auth_impl.data.remote.dto.AuthResponse
import ru.mareanex.auth_impl.data.remote.dto.LoginRequest
import ru.mareanex.auth_impl.data.remote.dto.RegisterRequest

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}
