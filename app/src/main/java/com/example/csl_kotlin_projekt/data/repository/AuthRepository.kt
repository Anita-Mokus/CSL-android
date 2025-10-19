package com.example.csl_kotlin_projekt.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.csl_kotlin_projekt.data.api.AuthApiService
import com.example.csl_kotlin_projekt.data.models.AuthResponseDto
import com.example.csl_kotlin_projekt.data.models.SignInDto
import com.example.csl_kotlin_projekt.data.models.SignUpDto
import com.example.csl_kotlin_projekt.data.models.TokensDto
import com.example.csl_kotlin_projekt.data.models.ProfileResponseDto
import com.example.csl_kotlin_projekt.data.models.UpdateProfileDto
import okhttp3.MultipartBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

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
        if (response.tokens == null || response.user == null) {
            println("ERROR: saveTokens called with incomplete data. Tokens: ${response.tokens}, User: ${response.user}")
            return
        }
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
        val refreshToken = getRefreshToken()
        return !refreshToken.isNullOrEmpty()
    }
    
    suspend fun signUp(username: String, email: String, password: String): Result<AuthResponseDto> = withContext(Dispatchers.IO) {
        try {
            val usernamePart = MultipartBody.Part.createFormData("username", username)
            val emailPart = MultipartBody.Part.createFormData("email", email)
            val passwordPart = MultipartBody.Part.createFormData("password", password)
            
            val response = authApiService.signUp(usernamePart, emailPart, passwordPart)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                if (authResponse.tokens == null || authResponse.user == null) {
                    return@withContext Result.failure(Exception("Registration successful, but server response was incomplete."))
                }
                saveTokens(authResponse)
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signIn(email: String, password: String): Result<AuthResponseDto> = withContext(Dispatchers.IO) {
        try {
            val request = SignInDto(email, password)
            val response = authApiService.signIn(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                if (authResponse.tokens == null || authResponse.user == null) {
                    return@withContext Result.failure(Exception("Login successful, but server response was incomplete."))
                }
                saveTokens(authResponse)
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = authApiService.logout()
            clearTokens() // Always clear local tokens regardless of API response
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Logout failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            clearTokens() // Clear tokens even if API call fails
            Result.failure(e)
        }
    }
    
    suspend fun refreshAccessToken(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = authApiService.refreshToken()

            if (response.isSuccessful && response.body() != null) {
                val tokens = response.body()!!
                // Manually update the tokens in SharedPreferences
                sharedPreferences.edit()
                    .putString(KEY_ACCESS_TOKEN, tokens.accessToken)
                    .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
                    .apply()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Token refresh failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(): Result<ProfileResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = authApiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(username: String?): Result<ProfileResponseDto> = withContext(Dispatchers.IO) {
        try {
            val dto = UpdateProfileDto(username = username)
            val response = authApiService.updateProfile(dto)
            if (response.isSuccessful && response.body() != null) {
                // Update cached username/email if present
                response.body()?.let { p ->
                    sharedPreferences.edit().putString(KEY_USERNAME, p.username).apply()
                }
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfileImage(imageBytes: ByteArray, filename: String = "profile.jpg", mimeType: String = "image/jpeg"): Result<ProfileResponseDto> = withContext(Dispatchers.IO) {
        try {
            val body = imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("profileImage", filename, body)
            val response = authApiService.uploadProfileImage(part)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to upload profile image: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension function to create AuthRepository
fun createAuthRepository(context: Context): AuthRepository {
    val sharedPreferences = context.getSharedPreferences(AuthRepository.PREF_NAME, Context.MODE_PRIVATE)
    val authApiService = com.example.csl_kotlin_projekt.data.network.NetworkModule.createAuthApiService(context)
    return AuthRepository(authApiService, sharedPreferences)
}
