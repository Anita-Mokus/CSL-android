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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class HomeUiState(
    val isLoading: Boolean = false,
    val username: String? = null,
    val email: String? = null,
    val profileImageUrl: String? = null,
    val profileImageBase64: String? = null,
    val logoutError: String? = null,
    val isLogoutSuccessful: Boolean = false,
    val schedule: List<ScheduleResponseDto> = emptyList(),
    val scheduleError: String? = null,
    val currentDate: Date = Date(),
    // Progress dialog state
    val showProgressDialog: Boolean = false,
    val progressScheduleId: Int? = null,
    val progressLoggedTime: String = "",
    val progressNotes: String = "",
    val progressCompleted: Boolean = false,
    val progressError: String? = null,
    val progressSubmitting: Boolean = false,
    // Track schedules currently toggling completion to disable UI while updating
    val togglingScheduleIds: Set<Int> = emptySet(),
    // Optimistic desired completion state while toggling
    val togglingDesired: Map<Int, Boolean> = emptyMap()
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadSchedule(context: android.content.Context, date: Date? = Date()) {
        _uiState.value = _uiState.value.copy(isLoading = true, scheduleError = null, currentDate = date ?: Date())
        viewModelScope.launch {
            val scheduleRepository = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val authRepository = createAuthRepository(context)
            val token = authRepository.getAccessToken()
            if (token.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    scheduleError = "You must be logged in to load schedules."
                )
                return@launch
            }

            // Use server-side filtered by day
            val result = scheduleRepository.getSchedulesByDay(date, token)
            if (result.isSuccess) {
                val list = result.getOrNull().orEmpty().sortedBy { it.startTime }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    schedule = list
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    scheduleError = result.exceptionOrNull()?.message ?: "Failed to load schedule"
                )
            }
        }
    }

    fun loadUserInfo(context: android.content.Context) {
        viewModelScope.launch {
            val authRepository = createAuthRepository(context)
            // Try to fetch fresh profile for image and authoritative username/email
            val res = authRepository.getProfile()
            if (res.isSuccess) {
                val p = res.getOrNull()!!
                _uiState.value = _uiState.value.copy(
                    username = p.username,
                    email = p.email,
                    profileImageUrl = p.profileImageUrl,
                    profileImageBase64 = p.profileImageBase64
                )
            } else {
                // Fallback to cached prefs for username/email; no image available
                _uiState.value = _uiState.value.copy(
                    username = authRepository.getUsername(),
                    email = authRepository.getEmail(),
                    profileImageUrl = null,
                    profileImageBase64 = null
                )
            }
        }
    }

    fun logout(context: android.content.Context) {
        _uiState.value = _uiState.value.copy(isLoading = true, logoutError = null)
        viewModelScope.launch {
            val authRepository = createAuthRepository(context)
            val result = authRepository.logout()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isLogoutSuccessful = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    logoutError = result.exceptionOrNull()?.message ?: "Logout failed"
                )
            }
        }
    }

    fun openProgressDialog(scheduleId: Int) {
        _uiState.value = _uiState.value.copy(
            showProgressDialog = true,
            progressScheduleId = scheduleId,
            progressLoggedTime = "",
            progressNotes = "",
            progressCompleted = false,
            progressError = null
        )
    }

    fun closeProgressDialog() {
        _uiState.value = _uiState.value.copy(
            showProgressDialog = false,
            progressScheduleId = null,
            progressLoggedTime = "",
            progressNotes = "",
            progressCompleted = false,
            progressSubmitting = false,
            progressError = null
        )
    }

    fun updateProgressLoggedTime(v: String) { _uiState.value = _uiState.value.copy(progressLoggedTime = v, progressError = null) }
    fun updateProgressNotes(v: String) { _uiState.value = _uiState.value.copy(progressNotes = v, progressError = null) }
    fun toggleProgressCompleted() { _uiState.value = _uiState.value.copy(progressCompleted = !_uiState.value.progressCompleted) }

    fun submitProgress(context: android.content.Context) {
        val s = _uiState.value
        val scheduleId = s.progressScheduleId ?: run {
            _uiState.value = s.copy(progressError = "No schedule selected")
            return
        }
        // Validate logged time if provided
        if (s.progressLoggedTime.isNotBlank()) {
            val n = s.progressLoggedTime.toIntOrNull()
            if (n == null || n < 0) {
                _uiState.value = s.copy(progressError = "Logged time must be a non-negative number")
                return
            }
        }

        _uiState.value = s.copy(progressSubmitting = true, progressError = null)
        viewModelScope.launch {
            val repo = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val auth = createAuthRepository(context)
            val token = auth.getAccessToken()
            if (token.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(progressSubmitting = false, progressError = "You must be logged in.")
                return@launch
            }
            // Use current local time ISO for the progress date
            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }.format(Date())
            val dto = com.example.csl_kotlin_projekt.data.models.CreateProgressDto(
                scheduleId = scheduleId,
                date = nowIso,
                loggedTime = s.progressLoggedTime.toIntOrNull(),
                notes = s.progressNotes.takeIf { it.isNotBlank() },
                isCompleted = s.progressCompleted
            )
            val result = repo.createProgress(token, dto)
            if (result.isSuccess) {
                // Refresh schedules for the currently selected day and close dialog
                loadSchedule(context, _uiState.value.currentDate)
                _uiState.value = _uiState.value.copy(progressSubmitting = false, showProgressDialog = false)
            } else {
                _uiState.value = _uiState.value.copy(progressSubmitting = false, progressError = result.exceptionOrNull()?.message ?: "Failed to add progress")
            }
        }
    }

    fun toggleScheduleCompleted(context: android.content.Context, scheduleId: Int, newCompleted: Boolean) {
        // mark as toggling to disable the checkbox and set desired state optimistically
        _uiState.value = _uiState.value.copy(
            togglingScheduleIds = _uiState.value.togglingScheduleIds + scheduleId,
            togglingDesired = _uiState.value.togglingDesired + (scheduleId to newCompleted)
        )
        viewModelScope.launch {
            val repo = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val token = createAuthRepository(context).getAccessToken()
            if (token.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    scheduleError = "You must be logged in.",
                    togglingScheduleIds = _uiState.value.togglingScheduleIds - scheduleId,
                    togglingDesired = _uiState.value.togglingDesired - scheduleId
                )
                return@launch
            }
            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }.format(Date())
            val dto = com.example.csl_kotlin_projekt.data.models.CreateProgressDto(
                scheduleId = scheduleId,
                date = nowIso,
                isCompleted = newCompleted
            )
            val result = repo.createProgress(token, dto)
            if (result.isSuccess) {
                loadSchedule(context, _uiState.value.currentDate)
                _uiState.value = _uiState.value.copy(
                    togglingScheduleIds = _uiState.value.togglingScheduleIds - scheduleId,
                    togglingDesired = _uiState.value.togglingDesired - scheduleId
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    scheduleError = result.exceptionOrNull()?.message ?: "Failed to update completion",
                    togglingScheduleIds = _uiState.value.togglingScheduleIds - scheduleId,
                    togglingDesired = _uiState.value.togglingDesired - scheduleId
                )
            }
        }
    }
}
