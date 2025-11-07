package com.example.csl_kotlin_projekt.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.csl_kotlin_projekt.util.AppLog
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.csl_kotlin_projekt.MyApp

data class SplashUiState(
    val isLoading: Boolean = true,
    val shouldNavigateToHome: Boolean = false,
    val shouldNavigateToLogin: Boolean = false,
    val error: String? = null
)

class SplashViewModel(private val authRepository: AuthRepository) : ViewModel() {
    init { AppLog.i("AL/SplashViewModel", "init") }

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
    
    fun checkAutoLogin() {
        viewModelScope.launch {
            try {
                // Minimum 3 seconds splash duration
                delay(3000)
                
                if (!authRepository.isLoggedIn()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        shouldNavigateToLogin = true
                    )
                    return@launch
                }
                
                // Try to refresh token to validate it and get new access token
                val refreshResult = authRepository.refreshAccessToken()

                if (refreshResult.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        shouldNavigateToHome = true
                    )
                } else {
                    // Token refresh failed, clear tokens and go to login
                    authRepository.clearTokens()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        shouldNavigateToLogin = true,
                        error = refreshResult.exceptionOrNull()?.message
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    shouldNavigateToLogin = true,
                    error = e.message
                )
            }
        }
    }
    
    fun resetNavigationState() {
        _uiState.value = _uiState.value.copy(
            shouldNavigateToHome = false,
            shouldNavigateToLogin = false,
            error = null
        )
    }

    override fun onCleared() {
        AppLog.i("AL/SplashViewModel", "onCleared")
        super.onCleared()
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = context.applicationContext as MyApp
                val c = app.container
                @Suppress("UNCHECKED_CAST")
                return SplashViewModel(c.authRepository) as T
            }
        }
    }
}
