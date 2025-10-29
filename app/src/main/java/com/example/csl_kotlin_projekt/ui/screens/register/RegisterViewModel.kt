package com.example.csl_kotlin_projekt.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.network.NetworkModule
import com.example.csl_kotlin_projekt.data.repository.createAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Patterns

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isRegistrationSuccessful: Boolean = false
)

class RegisterViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            usernameError = null,
            generalError = null
        )
    }
    
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
            confirmPasswordError = null,
            generalError = null
        )
    }
    
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null,
            generalError = null
        )
    }
    
    fun register(context: android.content.Context, onSuccess: () -> Unit) {
        val currentState = _uiState.value
        
        // Clear previous errors
        _uiState.value = currentState.copy(
            usernameError = null,
            emailError = null,
            passwordError = null,
            confirmPasswordError = null,
            generalError = null
        )
        
        // Validate inputs
        val usernameError = validateUsername(currentState.username)
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)
        val confirmPasswordError = validateConfirmPassword(currentState.password, currentState.confirmPassword)
        
        if (usernameError != null || emailError != null || passwordError != null || confirmPasswordError != null) {
            _uiState.value = currentState.copy(
                usernameError = usernameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
            return
        }
        
        // Start registration process
        _uiState.value = currentState.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val authRepository = createAuthRepository(context)
                val result = authRepository.signUp(
                    currentState.username,
                    currentState.email,
                    currentState.password
                )
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistrationSuccessful = true
                    )
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generalError = result.exceptionOrNull()?.message ?: "Registration failed"
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

    fun registerWithGoogle(context: android.content.Context, idToken: String, onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(generalError = null, isLoading = true)
        viewModelScope.launch {
            val repo = createAuthRepository(context)
            val result = repo.googleSignIn(idToken)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isRegistrationSuccessful = true)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, generalError = result.exceptionOrNull()?.message ?: "Google sign-in failed")
            }
        }
    }

    private fun validateUsername(username: String): String? {
        return when {
            username.isBlank() -> "Username is required"
            username.length < 3 -> "Username must be at least 3 characters"
            username.length > 20 -> "Username must be less than 20 characters"
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Username can only contain letters, numbers, and underscores"
            else -> null
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
            password.length > 50 -> "Password must be less than 50 characters"
            else -> null
        }
    }
    
    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Password confirmation is required"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }
    
    fun resetRegistrationSuccess() {
        _uiState.value = _uiState.value.copy(isRegistrationSuccessful = false)
    }
}
