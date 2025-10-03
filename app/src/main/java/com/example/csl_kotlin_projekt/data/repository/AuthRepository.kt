package com.example.csl_kotlin_projekt.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.csl_kotlin_projekt.data.api.AuthApiService
import com.example.csl_kotlin_projekt.data.models.AuthResponseDto
import com.example.csl_kotlin_projekt.data.models.TokenRefreshRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val authApiService: AuthApiService,
    private val sharedPreferences: SharedPreferences
) {
    
    companion object {
        const val PREF_NAME = "auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
    }
    
    fun getAccessToken(): String? = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    fun getUserId(): Int = sharedPreferences.getInt(KEY_USER_ID, -1)
    fun getUsername(): String? = sharedPreferences.getString(KEY_USERNAME, null)
    fun getEmail(): String? = sharedPreferences.getString(KEY_EMAIL, null)
    
    fun saveTokens(response: AuthResponseDto) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, response.tokens.accessToken)
            putString(KEY_REFRESH_TOKEN, response.tokens.refreshToken)
            putInt(KEY_USER_ID, response.user.id)
            putString(KEY_USERNAME, response.user.username)
            putString(KEY_EMAIL, response.user.email)
            apply()
        }
    }
    
    fun clearTokens() {
        sharedPreferences.edit().clear().apply()
    }
    
    fun isLoggedIn(): Boolean {
        val accessToken = getAccessToken()
        val refreshToken = getRefreshToken()
        return !accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()
    }
    
    suspend fun refreshAccessToken(): Result<AuthResponseDto> = withContext(Dispatchers.IO) {
        try {
            val refreshToken = getRefreshToken()
            if (refreshToken.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("No refresh token available"))
            }
            
            val request = TokenRefreshRequest(refreshToken)
            val response = authApiService.refreshToken(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                saveTokens(authResponse)
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Token refresh failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension function to create AuthRepository
fun createAuthRepository(context: Context, authApiService: AuthApiService): AuthRepository {
    val sharedPreferences = context.getSharedPreferences(AuthRepository.PREF_NAME, Context.MODE_PRIVATE)
    return AuthRepository(authApiService, sharedPreferences)
}
