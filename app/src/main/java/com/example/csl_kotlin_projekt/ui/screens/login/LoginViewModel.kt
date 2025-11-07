package com.example.csl_kotlin_projekt.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Patterns
import android.util.Log
import com.example.csl_kotlin_projekt.util.AppLog
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.csl_kotlin_projekt.MyApp

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoginSuccessful: Boolean = false
)

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

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

    fun setGeneralError(message: String?) {
        Log.e("LoginViewModel", "Error: ${message ?: "unknown"}")
        _uiState.value = _uiState.value.copy(generalError = message)
    }

    fun login(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        
        _uiState.value = currentState.copy(
            emailError = null,
            passwordError = null,
            generalError = null
        )
        
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)
        
        if (emailError != null || passwordError != null) {
            _uiState.value = currentState.copy(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
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

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(generalError = null, isLoading = true)
        viewModelScope.launch {
            val result = authRepository.googleSignIn(idToken)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isLoginSuccessful = true)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, generalError = result.exceptionOrNull()?.message ?: "Google sign-in failed")
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

    override fun onCleared() {
        AppLog.i("AL/LoginViewModel", "onCleared")
        super.onCleared()
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = context.applicationContext as MyApp
                val c = app.container
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(c.authRepository) as T
            }
        }
    }
}
