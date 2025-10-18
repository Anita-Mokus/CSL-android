package com.example.csl_kotlin_projekt.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.models.HabitResponseDto
import com.example.csl_kotlin_projekt.data.models.ProfileResponseDto
import com.example.csl_kotlin_projekt.data.network.NetworkModule
import com.example.csl_kotlin_projekt.data.repository.AuthRepository
import com.example.csl_kotlin_projekt.data.repository.ScheduleRepository
import com.example.csl_kotlin_projekt.data.repository.createAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val profile: ProfileResponseDto? = null,
    val habits: List<HabitResponseDto> = emptyList(),
    val showLogoutConfirm: Boolean = false,
    val loggingOut: Boolean = false,
    val logoutSuccess: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun load(context: android.content.Context) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val authRepo: AuthRepository = createAuthRepository(context)
            val scheduleRepo = ScheduleRepository(NetworkModule.createScheduleApiService(context))

            val profileRes = authRepo.getProfile()
            if (profileRes.isSuccess) {
                val profile = profileRes.getOrNull()!!
                _uiState.value = _uiState.value.copy(profile = profile)
                val token = authRepo.getAccessToken()
                if (token.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "You must be logged in.")
                    return@launch
                }
                val habitsRes = scheduleRepo.getHabitsByUser(token, profile.id)
                if (habitsRes.isSuccess) {
                    _uiState.value = _uiState.value.copy(isLoading = false, habits = habitsRes.getOrNull().orEmpty())
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = habitsRes.exceptionOrNull()?.message ?: "Failed to load habits")
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = profileRes.exceptionOrNull()?.message ?: "Failed to load profile")
            }
        }
    }

    fun openLogoutConfirm() { _uiState.value = _uiState.value.copy(showLogoutConfirm = true) }
    fun cancelLogout() { _uiState.value = _uiState.value.copy(showLogoutConfirm = false) }

    fun confirmLogout(context: android.content.Context) {
        _uiState.value = _uiState.value.copy(loggingOut = true, showLogoutConfirm = false, error = null)
        viewModelScope.launch {
            val repo = createAuthRepository(context)
            val res = repo.logout()
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(loggingOut = false, logoutSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(loggingOut = false, error = res.exceptionOrNull()?.message ?: "Failed to logout")
            }
        }
    }
}

