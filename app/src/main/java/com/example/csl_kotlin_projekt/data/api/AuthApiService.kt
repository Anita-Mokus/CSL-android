package com.example.csl_kotlin_projekt.data.api

import com.example.csl_kotlin_projekt.data.models.AuthResponseDto
import com.example.csl_kotlin_projekt.data.models.SignInDto
import com.example.csl_kotlin_projekt.data.models.SignUpDto
import com.example.csl_kotlin_projekt.data.models.TokenRefreshRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApiService {
    
    @Multipart
    @POST("auth/local/signup")
    suspend fun signUp(
        @Part username: MultipartBody.Part,
        @Part email: MultipartBody.Part,
        @Part password: MultipartBody.Part
    ): Response<AuthResponseDto>
    
    @POST("auth/local/signin")
    suspend fun signIn(
        @Body request: SignInDto
    ): Response<AuthResponseDto>
    
    @POST("auth/local/refresh")
    suspend fun refreshToken(
        @Body request: TokenRefreshRequest
    ): Response<AuthResponseDto>
    
    @POST("auth/local/logout")
    suspend fun logout(): Response<Unit>
}
