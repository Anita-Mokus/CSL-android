package com.example.csl_kotlin_projekt.ui.screens.createschedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.models.CreateCustomScheduleDto
import com.example.csl_kotlin_projekt.data.models.HabitResponseDto
import com.example.csl_kotlin_projekt.data.network.NetworkModule
import com.example.csl_kotlin_projekt.data.repository.ScheduleRepository
import com.example.csl_kotlin_projekt.data.repository.createAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CreateScheduleUiState(
    val habits: List<HabitResponseDto> = emptyList(),
    val selectedHabit: HabitResponseDto? = null,
    val startTime: Calendar = Calendar.getInstance(),
    val durationMinutes: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isScheduleCreated: Boolean = false
)

class CreateScheduleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateScheduleUiState())
    val uiState: StateFlow<CreateScheduleUiState> = _uiState.asStateFlow()

    fun loadHabits(context: android.content.Context) {
        Log.d("CreateScheduleVM", "loadHabits called")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val scheduleRepository = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val authRepository = createAuthRepository(context)
            val token = authRepository.getAccessToken()
            if (token.isNullOrBlank()) {
                Log.w("CreateScheduleVM", "No access token found; cannot load habits")
                _uiState.value = _uiState.value.copy(isLoading = false, error = "You must be logged in to load habits.")
                return@launch
            }
            Log.d("CreateScheduleVM", "Calling getAllHabits with token present")
            val result = scheduleRepository.getAllHabits(token)
            if (result.isSuccess) {
                val list = result.getOrNull().orEmpty()
                Log.d("CreateScheduleVM", "getAllHabits succeeded: count=${list.size}")
                _uiState.value = _uiState.value.copy(isLoading = false, habits = list)
            } else {
                val errMsg = result.exceptionOrNull()?.message ?: "Failed to load habits"
                Log.w("CreateScheduleVM", "getAllHabits failed: $errMsg")
                _uiState.value = _uiState.value.copy(isLoading = false, error = errMsg)
            }
        }
    }

    fun onHabitSelected(habit: HabitResponseDto) {
        _uiState.value = _uiState.value.copy(selectedHabit = habit)
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        val current = _uiState.value.startTime
        val newCal = (current.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        _uiState.value = _uiState.value.copy(startTime = newCal)
    }

    fun onDateSelected(year: Int, month: Int, day: Int) {
        val current = _uiState.value.startTime
        val newCal = (current.clone() as Calendar).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }
        _uiState.value = _uiState.value.copy(startTime = newCal)
    }

    fun onDurationChanged(duration: String) {
        _uiState.value = _uiState.value.copy(durationMinutes = duration)
    }

    fun onNotesChanged(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun createSchedule(context: android.content.Context) {
        if (_uiState.value.selectedHabit == null) {
            _uiState.value = _uiState.value.copy(error = "Please select a habit")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val scheduleRepository = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val authRepository = createAuthRepository(context)
            val token = authRepository.getAccessToken()
            if (token.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "You must be logged in to create a schedule.")
                return@launch
            }

            // Use ISO-8601 with timezone offset to avoid day boundary issues
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }
            val startIso = dateFormat.format(_uiState.value.startTime.time)
            Log.d("CreateScheduleVM", "Creating schedule at local ISO time=$startIso tz=${dateFormat.timeZone.id}")

            val scheduleDto = CreateCustomScheduleDto(
                habitId = _uiState.value.selectedHabit!!.id,
                date = startIso,
                startTime = startIso,
                // isCustom defaults to true
                endTime = null,
                durationMinutes = _uiState.value.durationMinutes.toIntOrNull(),
                participantIds = null,
                notes = _uiState.value.notes.takeIf { it.isNotBlank() }
            )

            val result = scheduleRepository.createCustomSchedule(token, scheduleDto)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isScheduleCreated = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to create schedule")
            }
        }
    }
}
