package com.example.csl_kotlin_projekt.data.api

import com.example.csl_kotlin_projekt.data.models.AuthResponseDto
import com.example.csl_kotlin_projekt.data.models.SignInDto
import com.example.csl_kotlin_projekt.data.models.SignUpDto
import com.example.csl_kotlin_projekt.data.models.TokensDto
import com.example.csl_kotlin_projekt.data.models.ProfileResponseDto
import com.example.csl_kotlin_projekt.data.models.UpdateProfileDto
import com.example.csl_kotlin_projekt.data.models.GoogleSignInDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PATCH

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

    @POST("auth/google")
    suspend fun googleSignIn(
        @Body request: GoogleSignInDto
    ): Response<AuthResponseDto>

    @POST("auth/local/refresh")
    suspend fun refreshToken(): Response<TokensDto>
    
    @POST("auth/local/logout")
    suspend fun logout(): Response<Unit>

    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponseDto>

    @PATCH("profile")
    suspend fun updateProfile(
        @Body dto: UpdateProfileDto
    ): Response<ProfileResponseDto>

    @Multipart
    @POST("profile/upload-profile-image")
    suspend fun uploadProfileImage(
        @Part profileImage: MultipartBody.Part
    ): Response<ProfileResponseDto>
}
