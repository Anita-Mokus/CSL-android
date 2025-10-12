package com.example.csl_kotlin_projekt.ui.screens.scheduledetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.models.ScheduleResponseDto
import com.example.csl_kotlin_projekt.data.network.NetworkModule
import com.example.csl_kotlin_projekt.data.repository.ScheduleRepository
import com.example.csl_kotlin_projekt.data.repository.createAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScheduleDetailsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val schedule: ScheduleResponseDto? = null
)

class ScheduleDetailsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScheduleDetailsUiState())
    val uiState: StateFlow<ScheduleDetailsUiState> = _uiState.asStateFlow()

    fun load(context: android.content.Context, id: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val repo = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val token = createAuthRepository(context).getAccessToken()
            if (token.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "You must be logged in.")
                return@launch
            }
            val result = repo.getScheduleById(token, id)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, schedule = result.getOrNull())
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to load schedule")
            }
        }
    }
}

