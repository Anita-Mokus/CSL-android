package com.example.csl_kotlin_projekt.data.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

object NetworkModule {

    const val BASE_URL = "http://192.168.1.56:8080/"
    //    private const val BASE_URL = "http://172.20.10.5:8080/"
    @Volatile private var okHttpClient: OkHttpClient? = null
    @Volatile private var retrofit: Retrofit? = null
    @Volatile private var authApi: com.example.csl_kotlin_projekt.data.api.AuthApiService? = null
    @Volatile private var scheduleApi: com.example.csl_kotlin_projekt.data.api.ScheduleApiService? = null

    private fun createAuthInterceptor(context: Context): Interceptor {
        val appCtx = context.applicationContext
        return Interceptor { chain ->
            val request = chain.request()
            if (request.header("Authorization") != null) {
                return@Interceptor chain.proceed(request)
            }
            val sharedPreferences = appCtx.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val isRefreshEndpoint = request.url.encodedPath.contains("/auth/local/refresh")
            val token = if (isRefreshEndpoint) {
                sharedPreferences.getString("refresh_token", null)
            } else {
                sharedPreferences.getString("access_token", null)
            }
            val newRequest = if (!token.isNullOrEmpty()) {
                request.newBuilder().addHeader("Authorization", "Bearer $token").build()
            } else request
            chain.proceed(newRequest)
        }
    }

    private fun getOrCreateOkHttpClient(context: Context): OkHttpClient {
        val existing = okHttpClient
        if (existing != null) return existing
        synchronized(this) {
            val again = okHttpClient
            if (again != null) return again
            val client = OkHttpClient.Builder()
                .addInterceptor(createAuthInterceptor(context.applicationContext))
                .authenticator(TokenAuthenticator(context.applicationContext))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            okHttpClient = client
            return client
        }
    }

    private fun getOrCreateRetrofit(context: Context): Retrofit {
        val existing = retrofit
        if (existing != null) return existing
        synchronized(this) {
            val again = retrofit
            if (again != null) return again
            val r = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getOrCreateOkHttpClient(context.applicationContext))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            retrofit = r
            return r
        }
    }

    fun createAuthApiService(context: Context): com.example.csl_kotlin_projekt.data.api.AuthApiService {
        val existing = authApi
        if (existing != null) return existing
        synchronized(this) {
            val again = authApi
            if (again != null) return again
            val svc = getOrCreateRetrofit(context.applicationContext).create(com.example.csl_kotlin_projekt.data.api.AuthApiService::class.java)
            authApi = svc
            return svc
        }
    }

    fun createScheduleApiService(context: Context): com.example.csl_kotlin_projekt.data.api.ScheduleApiService {
        val existing = scheduleApi
        if (existing != null) return existing
        synchronized(this) {
            val again = scheduleApi
            if (again != null) return again
            val svc = getOrCreateRetrofit(context.applicationContext).create(com.example.csl_kotlin_projekt.data.api.ScheduleApiService::class.java)
            scheduleApi = svc
            return svc
        }
    }
}

private class TokenAuthenticator(private val context: Context) : okhttp3.Authenticator {
    override fun authenticate(route: okhttp3.Route?, response: Response): okhttp3.Request? {
        if (response.priorResponse != null && response.priorResponse?.code == 401) {
            return null
        }
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val refreshToken = prefs.getString("refresh_token", null) ?: return null
        val newTokens = try { refreshTokens(refreshToken) } catch (_: Exception) { null } ?: return null
        prefs.edit()
            .putString("access_token", newTokens.accessToken)
            .putString("refresh_token", newTokens.refreshToken)
            .apply()
        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newTokens.accessToken}")
            .build()
    }

    private fun refreshTokens(refreshToken: String): com.example.csl_kotlin_projekt.data.models.TokensDto? {
        val client = OkHttpClient.Builder().build()
        val url = NetworkModule.BASE_URL + "auth/local/refresh"
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
            } catch (_: Exception) { null }
        }
    }
}
