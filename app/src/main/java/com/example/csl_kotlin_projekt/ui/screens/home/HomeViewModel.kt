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
import java.util.Date

data class HomeUiState(
    val isLoading: Boolean = false,
    val username: String? = null,
    val email: String? = null,
    val logoutError: String? = null,
    val isLogoutSuccessful: Boolean = false,
    val schedule: List<ScheduleResponseDto> = emptyList(),
    val scheduleError: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadSchedule(context: android.content.Context, date: Date? = Date()) {
        _uiState.value = _uiState.value.copy(isLoading = true, scheduleError = null)
        viewModelScope.launch {
            val scheduleRepository = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val authRepository = createAuthRepository(context)
            val token = authRepository.getAccessToken()
            if (token.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    scheduleError = "You must be logged in to load today’s schedule."
                )
                return@launch
            }

            // Fetch all schedules and filter client-side for today's local day
            val result = scheduleRepository.getAllSchedules(token)
            if (result.isSuccess) {
                val all = result.getOrNull().orEmpty()
                val cal = java.util.Calendar.getInstance().apply {
                    time = date ?: Date()
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                val start = cal.time
                cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
                val end = cal.time
                val todays = all.filter { it.startTime >= start && it.startTime < end }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    schedule = todays.sortedBy { it.startTime }
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
            _uiState.value = _uiState.value.copy(
                username = authRepository.getUsername(),
                email = authRepository.getEmail()
            )
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
}
