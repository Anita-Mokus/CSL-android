import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.models.ScheduleResponseDto
import com.example.csl_kotlin_projekt.data.network.NetworkModule
import com.example.csl_kotlin_projekt.data.repository.AuthRepository
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

    init {
        // This is a placeholder for the context. In a real app, you would inject the context
        // or use an application context provided by a dependency injection framework.
        // For now, we'll call loadSchedule from the composable.
    }

    fun loadSchedule(context: android.content.Context, date: Date? = null) {
        _uiState.value = _uiState.value.copy(isLoading = true, scheduleError = null)
        viewModelScope.launch {
            val scheduleRepository = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val result = scheduleRepository.getSchedulesByDay(date)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    schedule = result.getOrNull()?.sortedBy { it.startTime } ?: emptyList()
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
