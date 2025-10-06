package com.example.csl_kotlin_projekt.data.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    
    private const val BASE_URL = "http://192.168.1.57:8080/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // Authentication interceptor to add the correct token to requests
    private fun createAuthInterceptor(context: Context): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

            val isRefreshEndpoint = request.url.encodedPath.contains("/auth/local/refresh")

            val token = if (isRefreshEndpoint) {
                // For the refresh endpoint, use the refresh token
                sharedPreferences.getString("refresh_token", null)
            } else {
                // For all other endpoints, use the access token
                sharedPreferences.getString("access_token", null)
            }

            val newRequest = if (token != null && !token.isEmpty()) {
                request.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                request
            }

            chain.proceed(newRequest)
        }
    }
    
    fun createOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(createAuthInterceptor(context))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    fun createRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun createAuthApiService(context: Context): com.example.csl_kotlin_projekt.data.api.AuthApiService {
        return createRetrofit(context).create(com.example.csl_kotlin_projekt.data.api.AuthApiService::class.java)
    }
}
