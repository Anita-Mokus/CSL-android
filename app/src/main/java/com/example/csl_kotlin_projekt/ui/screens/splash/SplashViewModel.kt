package com.example.csl_kotlin_projekt.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.network.NetworkModule
import com.example.csl_kotlin_projekt.data.repository.createAuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SplashUiState(
    val isLoading: Boolean = true,
    val shouldNavigateToHome: Boolean = false,
    val shouldNavigateToLogin: Boolean = false,
    val error: String? = null
)

class SplashViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
    
    fun checkAutoLogin(context: android.content.Context) {
        viewModelScope.launch {
            try {
                // Minimum 3 seconds splash duration
                delay(3000)
                
                val authRepository = createAuthRepository(context)
                
                if (!authRepository.isLoggedIn()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        shouldNavigateToLogin = true
                    )
                    return@launch
                }
                
                // Try to refresh token to validate it
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
                        shouldNavigateToLogin = true
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
}
