package com.example.csl_kotlin_projekt.ui.screens.createschedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.models.CreateCustomScheduleDto
import com.example.csl_kotlin_projekt.data.models.CreateRecurringScheduleDto
import com.example.csl_kotlin_projekt.data.models.CreateWeekdayRecurringScheduleDto
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
    // Recurring fields
    val repeatPattern: String = "none", // none|daily|weekdays|weekends
    val repeatDays: String = "30",
    val daysOfWeek: Set<Int> = emptySet(), // 1..7
    val numberOfWeeks: String = "4",

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

    fun onDurationChanged(duration: String) { _uiState.value = _uiState.value.copy(durationMinutes = duration) }
    fun onNotesChanged(notes: String) { _uiState.value = _uiState.value.copy(notes = notes) }

    fun onRepeatPatternChanged(pattern: String) { _uiState.value = _uiState.value.copy(repeatPattern = pattern) }
    fun onRepeatDaysChanged(days: String) { _uiState.value = _uiState.value.copy(repeatDays = days) }
    fun onToggleDayOfWeek(d: Int) {
        val current = _uiState.value.daysOfWeek
        _uiState.value = _uiState.value.copy(daysOfWeek = if (current.contains(d)) current - d else current + d)
    }
    fun onNumberOfWeeksChanged(weeks: String) { _uiState.value = _uiState.value.copy(numberOfWeeks = weeks) }

    private fun buildStartIso(): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        return df.format(_uiState.value.startTime.time)
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

            val startIso = buildStartIso()
            Log.d("CreateScheduleVM", "Creating schedule at local ISO time=$startIso tz=${TimeZone.getDefault().id}")

            val scheduleDto = CreateCustomScheduleDto(
                habitId = _uiState.value.selectedHabit!!.id,
                date = startIso,
                startTime = startIso,
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

    fun createRecurringSchedule(context: android.content.Context) {
        val s = _uiState.value
        if (s.selectedHabit == null) { _uiState.value = s.copy(error = "Please select a habit"); return }
        if (s.repeatPattern == "none") { _uiState.value = s.copy(error = "Select a repeat pattern other than 'none'"); return }

        _uiState.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val repo = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val token = createAuthRepository(context).getAccessToken()
            if (token.isNullOrBlank()) { _uiState.value = _uiState.value.copy(isLoading = false, error = "You must be logged in."); return@launch }

            val startIso = buildStartIso()
            val dto = CreateRecurringScheduleDto(
                habitId = s.selectedHabit.id,
                startTime = startIso,
                repeatPattern = s.repeatPattern,
                isCustom = true,
                endTime = null,
                durationMinutes = s.durationMinutes.toIntOrNull(),
                repeatDays = s.repeatDays.toIntOrNull(),
                participantIds = null,
                notes = s.notes.takeIf { it.isNotBlank() }
            )
            val result = repo.createRecurringSchedule(token, dto)
            if (result.isSuccess) {
                Log.d("CreateScheduleVM", "createRecurringSchedule success count=${result.getOrNull()?.size}")
                _uiState.value = _uiState.value.copy(isLoading = false, isScheduleCreated = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to create recurring schedules")
            }
        }
    }

    fun createWeekdayRecurringSchedule(context: android.content.Context) {
        val s = _uiState.value
        if (s.selectedHabit == null) { _uiState.value = s.copy(error = "Please select a habit"); return }
        if (s.daysOfWeek.isEmpty()) { _uiState.value = s.copy(error = "Select at least one weekday"); return }

        _uiState.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val repo = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val token = createAuthRepository(context).getAccessToken()
            if (token.isNullOrBlank()) { _uiState.value = _uiState.value.copy(isLoading = false, error = "You must be logged in."); return@launch }

            val startIso = buildStartIso()
            val dto = CreateWeekdayRecurringScheduleDto(
                habitId = s.selectedHabit.id,
                startTime = startIso,
                daysOfWeek = s.daysOfWeek.sorted(),
                numberOfWeeks = s.numberOfWeeks.toIntOrNull() ?: 4,
                durationMinutes = s.durationMinutes.toIntOrNull(),
                endTime = null,
                participantIds = null,
                notes = s.notes.takeIf { it.isNotBlank() }
            )
            val result = repo.createWeekdayRecurringSchedule(token, dto)
            if (result.isSuccess) {
                Log.d("CreateScheduleVM", "createWeekdayRecurringSchedule success count=${result.getOrNull()?.size}")
                _uiState.value = _uiState.value.copy(isLoading = false, isScheduleCreated = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to create weekday recurring schedules")
            }
        }
    }
}
