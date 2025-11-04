package com.example.csl_kotlin_projekt.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.models.HabitResponseDto
import com.example.csl_kotlin_projekt.data.models.ProfileResponseDto
import com.example.csl_kotlin_projekt.data.models.ScheduleStatus
import com.example.csl_kotlin_projekt.data.network.NetworkModule
import com.example.csl_kotlin_projekt.data.repository.AuthRepository
import com.example.csl_kotlin_projekt.data.repository.ScheduleRepository
import com.example.csl_kotlin_projekt.data.repository.createAuthRepository
import com.example.csl_kotlin_projekt.util.AppLog
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// Small UI model summarizing per-habit completion
data class HabitProgressUi(
    val habit: HabitResponseDto,
    val totalSchedules: Int,
    val completedSchedules: Int,
    val totalDurationMinutes: Int,
    val totalLoggedMinutes: Int
) {
    // Prefer minutes-based percentage if we have any planned duration; otherwise fall back to schedule completion ratio
    val percent: Float get() {
        return when {
            totalDurationMinutes > 0 -> (totalLoggedMinutes.toFloat() / totalDurationMinutes).coerceIn(0f, 1f)
            totalSchedules > 0 -> (completedSchedules.toFloat() / totalSchedules).coerceIn(0f, 1f)
            else -> 0f
        }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val profile: ProfileResponseDto? = null,
    val habits: List<HabitResponseDto> = emptyList(),
    val showLogoutConfirm: Boolean = false,
    val loggingOut: Boolean = false,
    val logoutSuccess: Boolean = false,
    // New: per-habit completion summaries rendered in the Profile screen
    val habitProgress: List<HabitProgressUi> = emptyList()
)

class ProfileViewModel : ViewModel() {
    init { AppLog.i("AL/ProfileViewModel", "init") }
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
                // Fetch habits and schedules in parallel to reduce wait time
                val (habitsRes, schedulesRes) = coroutineScope {
                    val a = async { scheduleRepo.getHabitsByUser(profile.id) }
                    val b = async { scheduleRepo.getAllSchedules() }
                    Pair(a.await(), b.await())
                }
                if (habitsRes.isSuccess) {
                    val habits = habitsRes.getOrNull().orEmpty()
                    val habitProgress = if (schedulesRes.isSuccess) {
                        val schedules = schedulesRes.getOrNull().orEmpty()
                        // Group schedules by habit id and compute completion counts and minutes
                        val byHabit = schedules.groupBy { it.habit.id }
                        habits.map { h ->
                            val list = byHabit[h.id].orEmpty()
                            val total = list.size
                            var completed = 0
                            var totalDuration = 0
                            var totalLogged = 0
                            list.forEach { s ->
                                // Count completed
                                val anyCompleted = s.status == ScheduleStatus.Completed || (s.progress?.any { it.isCompleted } == true)
                                if (anyCompleted) completed++
                                // Planned duration
                                val plannedMinutes = s.durationMinutes ?: run {
                                    val start = s.startTime.time
                                    val end = s.endTime?.time
                                    if (end != null && end > start) {
                                        val diffMs = end - start
                                        (TimeUnit.MILLISECONDS.toMinutes(diffMs)).toInt()
                                    } else 0
                                }
                                totalDuration += (plannedMinutes).coerceAtLeast(0)
                                // Logged minutes across all progress entries for this schedule
                                val logged = s.progress?.sumOf { it.loggedTime ?: 0 } ?: 0
                                totalLogged += logged.coerceAtLeast(0)
                            }
                            HabitProgressUi(
                                habit = h,
                                totalSchedules = total,
                                completedSchedules = completed,
                                totalDurationMinutes = totalDuration,
                                totalLoggedMinutes = totalLogged
                            )
                        }
                    } else {
                        // If schedules failed to load, still present habits with 0% (and bubble error)
                        _uiState.value = _uiState.value.copy(error = schedulesRes.exceptionOrNull()?.message ?: _uiState.value.error)
                        habits.map { h -> HabitProgressUi(h, totalSchedules = 0, completedSchedules = 0, totalDurationMinutes = 0, totalLoggedMinutes = 0) }
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, habits = habits, habitProgress = habitProgress)
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

    override fun onCleared() {
        AppLog.i("AL/ProfileViewModel", "onCleared")
        super.onCleared()
    }
}
