package com.example.csl_kotlin_projekt.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.network.NetworkModule
import com.example.csl_kotlin_projekt.data.repository.createAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Patterns

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoginSuccessful: Boolean = false
)

class LoginViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null,
            generalError = null
        )
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null,
            generalError = null
        )
    }
    
    fun login(context: android.content.Context, onSuccess: () -> Unit) {
        val currentState = _uiState.value
        
        // Clear previous errors
        _uiState.value = currentState.copy(
            emailError = null,
            passwordError = null,
            generalError = null
        )
        
        // Validate inputs
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)
        
        if (emailError != null || passwordError != null) {
            _uiState.value = currentState.copy(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }
        
        // Start login process
        _uiState.value = currentState.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val authRepository = createAuthRepository(context, NetworkModule.authApiService)
                val result = authRepository.signIn(currentState.email, currentState.password)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true
                    )
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generalError = result.exceptionOrNull()?.message ?: "Login failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generalError = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
    
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Please enter a valid email address"
            else -> null
        }
    }
    
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }
    
    fun resetLoginSuccess() {
        _uiState.value = _uiState.value.copy(isLoginSuccessful = false)
    }
}
