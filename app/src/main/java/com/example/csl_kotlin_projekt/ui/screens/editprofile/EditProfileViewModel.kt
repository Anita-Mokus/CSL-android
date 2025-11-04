package com.example.csl_kotlin_projekt.ui.screens.editprofile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.models.ProfileResponseDto
import com.example.csl_kotlin_projekt.data.repository.AuthRepository
import com.example.csl_kotlin_projekt.data.repository.createAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.csl_kotlin_projekt.util.AppLog

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val saving: Boolean = false,
    val uploading: Boolean = false,
    val profile: ProfileResponseDto? = null,
    val usernameInput: String = "",
    val email: String = "",
    val selectedImagePreviewUri: String? = null,
    val saveSuccess: Boolean = false
)

class EditProfileViewModel : ViewModel() {
    init { AppLog.i("AL/EditProfileViewModel", "init") }
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var repo: AuthRepository? = null

    fun load(context: Context) {
        if (repo == null) repo = createAuthRepository(context)
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val r = repo!!.getProfile()
            if (r.isSuccess) {
                val p = r.getOrNull()!!
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    profile = p,
                    usernameInput = p.username,
                    email = p.email
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = r.exceptionOrNull()?.message ?: "Failed to load profile")
            }
        }
    }

    fun setUsername(value: String) {
        _uiState.value = _uiState.value.copy(usernameInput = value)
    }

    fun saveUsername(context: Context) {
        val newName = _uiState.value.usernameInput.trim()
        _uiState.value = _uiState.value.copy(saving = true, error = null, saveSuccess = false)
        viewModelScope.launch {
            val r = repo?.updateProfile(newName)
            if (r?.isSuccess == true) {
                _uiState.value = _uiState.value.copy(saving = false, profile = r.getOrNull(), saveSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(saving = false, error = r?.exceptionOrNull()?.message ?: "Failed to update profile")
            }
        }
    }

    fun uploadImage(context: Context, bytes: ByteArray, filename: String, mimeType: String) {
        _uiState.value = _uiState.value.copy(uploading = true, error = null)
        viewModelScope.launch {
            val r = repo?.uploadProfileImage(bytes, filename, mimeType)
            if (r?.isSuccess == true) {
                _uiState.value = _uiState.value.copy(uploading = false, profile = r.getOrNull())
            } else {
                _uiState.value = _uiState.value.copy(uploading = false, error = r?.exceptionOrNull()?.message ?: "Failed to upload image")
            }
        }
    }

    override fun onCleared() {
        AppLog.i("AL/EditProfileViewModel", "onCleared")
        super.onCleared()
    }
}
