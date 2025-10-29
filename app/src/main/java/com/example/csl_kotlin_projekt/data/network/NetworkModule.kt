package com.example.csl_kotlin_projekt.data.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

object NetworkModule {

//    private const val BASE_URL = "http://172.20.10.5:8080/"
    private const val BASE_URL = "http://192.168.1.56:8080/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // Authentication interceptor to add the correct token to requests
    private fun createAuthInterceptor(context: Context): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()

            // If the Authorization header is already present (e.g., passed explicitly), don't add another
            if (request.header("Authorization") != null) {
                return@Interceptor chain.proceed(request)
            }

            val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

            val isRefreshEndpoint = request.url.encodedPath.contains("/auth/local/refresh")

            val token = if (isRefreshEndpoint) {
                // For the refresh endpoint, use the refresh token
                sharedPreferences.getString("refresh_token", null)
            } else {
                // For all other endpoints, use the access token
                sharedPreferences.getString("access_token", null)
            }

            val newRequest = if (token != null && token.isNotEmpty()) {
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
            .authenticator(TokenAuthenticator(context))
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

    fun createScheduleApiService(context: Context): com.example.csl_kotlin_projekt.data.api.ScheduleApiService {
        return createRetrofit(context).create(com.example.csl_kotlin_projekt.data.api.ScheduleApiService::class.java)
    }
}

// Token authenticator that refreshes access tokens on 401 and retries the request
private class TokenAuthenticator(private val context: Context) : okhttp3.Authenticator {
    override fun authenticate(route: okhttp3.Route?, response: Response): okhttp3.Request? {
        // Give up if we've already attempted to authenticate to avoid infinite loops
        if (response.priorResponse != null && response.priorResponse?.code == 401) {
            return null
        }

        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val refreshToken = prefs.getString("refresh_token", null) ?: return null

        // Synchronously refresh tokens
        val newTokens = try {
            refreshTokens(refreshToken)
        } catch (_: Exception) {
            null
        } ?: return null

        // Persist new tokens
        prefs.edit()
            .putString("access_token", newTokens.accessToken)
            .putString("refresh_token", newTokens.refreshToken)
            .apply()

        // Retry the request with the new access token
        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newTokens.accessToken}")
            .build()
    }

    private fun refreshTokens(refreshToken: String): com.example.csl_kotlin_projekt.data.models.TokensDto? {
        // Build a lightweight client without authenticator to avoid recursion
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()

        val url = NetworkModule.javaClass.getDeclaredField("BASE_URL").let { field ->
            field.isAccessible = true
            val base = field.get(NetworkModule) as String
            base + "auth/local/refresh"
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = "".toRequestBody(mediaType)

        val request = okhttp3.Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization", "Bearer $refreshToken")
            .build()

        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val json = resp.body?.string() ?: return null
            return try {
                com.google.gson.Gson().fromJson(json, com.example.csl_kotlin_projekt.data.models.TokensDto::class.java)
            } catch (_: Exception) {
                null
            }
        }
    }
}
