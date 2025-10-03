package com.example.csl_kotlin_projekt.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.network.NetworkModule
import com.example.csl_kotlin_projekt.data.repository.createAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val username: String? = null,
    val email: String? = null,
    val logoutError: String? = null
)

class HomeViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    fun loadUserInfo(context: android.content.Context) {
        viewModelScope.launch {
            val authRepository = createAuthRepository(context)
            _uiState.value = _uiState.value.copy(
                username = authRepository.getUsername(),
                email = authRepository.getEmail()
            )
        }
    }
    
    fun logout(context: android.content.Context, onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            logoutError = null
        )
        
        viewModelScope.launch {
            try {
                val authRepository = createAuthRepository(context)
                val result = authRepository.logout()
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        logoutError = result.exceptionOrNull()?.message ?: "Logout failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    logoutError = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
    
    fun clearLogoutError() {
        _uiState.value = _uiState.value.copy(logoutError = null)
    }
}
