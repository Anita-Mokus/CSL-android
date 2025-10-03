package com.example.csl_kotlin_projekt.data.api

import com.example.csl_kotlin_projekt.data.models.AuthResponseDto
import com.example.csl_kotlin_projekt.data.models.TokenRefreshRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    
    @POST("auth/local/refresh")
    suspend fun refreshToken(
        @Body request: TokenRefreshRequest
    ): Response<AuthResponseDto>
}
