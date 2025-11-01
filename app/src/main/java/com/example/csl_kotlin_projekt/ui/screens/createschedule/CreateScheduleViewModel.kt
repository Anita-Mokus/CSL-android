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

    // Field-level errors
    val habitError: String? = null,
    val durationError: String? = null,
    val repeatPatternError: String? = null,
    val weekdaysError: String? = null,

    // API/general error
    val error: String? = null,
    val isLoading: Boolean = false,
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
            val result = scheduleRepository.getAllHabits()
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
        _uiState.value = _uiState.value.copy(selectedHabit = habit, habitError = null, error = null)
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
        val err = duration.takeIf { it.isNotBlank() }?.toIntOrNull()?.let { if (it <= 0) "Duration must be a positive number" else null } ?: run {
            // Blank is allowed (optional); no error
            null
        } ?: if (duration.isNotBlank() && duration.toIntOrNull() == null) "Duration must be a number" else null
        _uiState.value = _uiState.value.copy(durationMinutes = duration, durationError = err, error = null)
    }
    fun onNotesChanged(notes: String) { _uiState.value = _uiState.value.copy(notes = notes, error = null) }

    fun onRepeatPatternChanged(pattern: String) { _uiState.value = _uiState.value.copy(repeatPattern = pattern, repeatPatternError = null, error = null) }
    fun onRepeatDaysChanged(days: String) { _uiState.value = _uiState.value.copy(repeatDays = days, error = null) }
    fun onToggleDayOfWeek(d: Int) {
        val current = _uiState.value.daysOfWeek
        val next = if (current.contains(d)) current - d else current + d
        _uiState.value = _uiState.value.copy(daysOfWeek = next, weekdaysError = null, error = null)
    }
    fun onNumberOfWeeksChanged(weeks: String) { _uiState.value = _uiState.value.copy(numberOfWeeks = weeks, error = null) }

    private fun buildStartIso(): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        return df.format(_uiState.value.startTime.time)
    }

    fun createSchedule(context: android.content.Context) {
        val s = _uiState.value
        // Field checks
        if (s.selectedHabit == null) {
            _uiState.value = s.copy(habitError = "Please select a habit", error = null)
            return
        }
        if (!s.durationMinutes.isBlank()) {
            val num = s.durationMinutes.toIntOrNull()
            if (num == null) { _uiState.value = s.copy(durationError = "Duration must be a number", error = null); return }
            if (num <= 0) { _uiState.value = s.copy(durationError = "Duration must be a positive number", error = null); return }
        }

        _uiState.value = s.copy(isLoading = true, error = null)
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
                habitId = s.selectedHabit.id,
                date = startIso,
                startTime = startIso,
                endTime = null,
                durationMinutes = s.durationMinutes.toIntOrNull(),
                participantIds = null,
                notes = s.notes.takeIf { it.isNotBlank() }
            )

            val result = scheduleRepository.createCustomSchedule(scheduleDto)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isScheduleCreated = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to create schedule")
            }
        }
    }

    // Some backends expect Sunday as 0 instead of 7. Map 1..7 (Mon..Sun) to backend values.
    // Mon..Sat stay the same if backend uses 1..6, Sunday(7) becomes 0.
    private fun mapDaysForBackend(days: Collection<Int>): List<Int> = days.map { d -> if (d == 7) 0 else d }.sorted()

    fun createRecurringSchedule(context: android.content.Context) {
        val s = _uiState.value
        if (s.selectedHabit == null) { _uiState.value = s.copy(habitError = "Please select a habit", error = null); return }
        if (s.repeatPattern == "none") { _uiState.value = s.copy(repeatPatternError = "Please choose a repeat pattern", error = null); return }
        if (!s.durationMinutes.isBlank()) {
            val num = s.durationMinutes.toIntOrNull()
            if (num == null) { _uiState.value = s.copy(durationError = "Duration must be a number", error = null); return }
            if (num <= 0) { _uiState.value = s.copy(durationError = "Duration must be a positive number", error = null); return }
        }

        _uiState.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val repo = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val token = createAuthRepository(context).getAccessToken()
            if (token.isNullOrBlank()) { _uiState.value = _uiState.value.copy(isLoading = false, error = "You must be logged in."); return@launch }

            val startIso = buildStartIso()

            // If the selected pattern is "weekends", use the weekdays endpoint with Sat and Sun,
            // mapping Sunday(7) -> 0 for backend compatibility.
            if (s.repeatPattern == "weekends") {
                val weekdayDto = CreateWeekdayRecurringScheduleDto(
                    habitId = s.selectedHabit.id,
                    startTime = startIso,
                    daysOfWeek = mapDaysForBackend(listOf(6, 7)),
                    numberOfWeeks = s.numberOfWeeks.toIntOrNull() ?: 4,
                    durationMinutes = s.durationMinutes.toIntOrNull(),
                    endTime = null,
                    participantIds = null,
                    notes = s.notes.takeIf { it.isNotBlank() }
                )
                val result = repo.createWeekdayRecurringSchedule(weekdayDto)
                if (result.isSuccess) {
                    Log.d("CreateScheduleVM", "createWeekendRecurringSchedule via weekdays endpoint success count=${result.getOrNull()?.size}")
                    _uiState.value = _uiState.value.copy(isLoading = false, isScheduleCreated = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to create weekend recurring schedules")
                }
                return@launch
            }

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
            val result = repo.createRecurringSchedule(dto)
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
        if (s.selectedHabit == null) { _uiState.value = s.copy(habitError = "Please select a habit", error = null); return }
        if (s.daysOfWeek.isEmpty()) { _uiState.value = s.copy(weekdaysError = "Select at least one weekday", error = null); return }
        if (!s.durationMinutes.isBlank()) {
            val num = s.durationMinutes.toIntOrNull()
            if (num == null) { _uiState.value = s.copy(durationError = "Duration must be a number", error = null); return }
            if (num <= 0) { _uiState.value = s.copy(durationError = "Duration must be a positive number", error = null); return }
        }

        _uiState.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val repo = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val token = createAuthRepository(context).getAccessToken()
            if (token.isNullOrBlank()) { _uiState.value = s.copy(isLoading = false, error = "You must be logged in."); return@launch }

            val startIso = buildStartIso()
            val dto = CreateWeekdayRecurringScheduleDto(
                habitId = s.selectedHabit.id,
                startTime = startIso,
                daysOfWeek = mapDaysForBackend(s.daysOfWeek),
                numberOfWeeks = s.numberOfWeeks.toIntOrNull() ?: 4,
                durationMinutes = s.durationMinutes.toIntOrNull(),
                endTime = null,
                participantIds = null,
                notes = s.notes.takeIf { it.isNotBlank() }
            )
            val result = repo.createWeekdayRecurringSchedule(dto)
            if (result.isSuccess) {
                Log.d("CreateScheduleVM", "createWeekdayRecurringSchedule success count=${result.getOrNull()?.size}")
                _uiState.value = _uiState.value.copy(isLoading = false, isScheduleCreated = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to create weekday recurring schedules")
            }
        }
    }
}
