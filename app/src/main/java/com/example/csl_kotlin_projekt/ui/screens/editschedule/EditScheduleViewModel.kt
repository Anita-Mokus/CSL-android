package com.example.csl_kotlin_projekt.ui.screens.editschedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.models.ScheduleResponseDto
import com.example.csl_kotlin_projekt.data.models.ScheduleStatus
import com.example.csl_kotlin_projekt.data.models.UpdateScheduleDto
import com.example.csl_kotlin_projekt.data.network.NetworkModule
import com.example.csl_kotlin_projekt.data.repository.ScheduleRepository
import com.example.csl_kotlin_projekt.data.repository.createAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class EditScheduleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val saving: Boolean = false,
    val schedule: ScheduleResponseDto? = null,
    val startTimeText: String = "",
    val endTimeText: String = "",
    val durationText: String = "",
    val status: ScheduleStatus = ScheduleStatus.Planned,
    val participantsText: String = "",
    val notesText: String = ""
)

class EditScheduleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditScheduleUiState())
    val uiState: StateFlow<EditScheduleUiState> = _uiState.asStateFlow()

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
                val s = result.getOrNull()!!
                val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                val startText = timeFmt.format(s.startTime)
                val endText = s.endTime?.let { timeFmt.format(it) } ?: ""
                val duration = s.durationMinutes?.toString() ?: ""
                val participants = (s.participants ?: emptyList()).joinToString(",") { it.id.toString() }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    schedule = s,
                    startTimeText = startText,
                    endTimeText = endText,
                    durationText = duration,
                    status = s.status,
                    participantsText = participants,
                    notesText = s.notes ?: ""
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to load schedule")
            }
        }
    }

    fun setStartTime(v: String) { _uiState.value = _uiState.value.copy(startTimeText = v) }
    fun setEndTime(v: String) { _uiState.value = _uiState.value.copy(endTimeText = v) }
    fun setDuration(v: String) { _uiState.value = _uiState.value.copy(durationText = v) }
    fun setStatus(v: ScheduleStatus) { _uiState.value = _uiState.value.copy(status = v) }
    fun setParticipants(v: String) { _uiState.value = _uiState.value.copy(participantsText = v) }
    fun setNotes(v: String) { _uiState.value = _uiState.value.copy(notesText = v) }

    private fun combineToIso(baseDate: java.util.Date, timeText: String): String? {
        // Expecting HH:mm
        val parts = timeText.trim().split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        val cal = Calendar.getInstance().apply {
            time = baseDate
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        return df.format(cal.time)
    }

    fun save(context: android.content.Context, onSaved: (ScheduleResponseDto) -> Unit) {
        val s = _uiState.value
        val loaded = s.schedule ?: run {
            _uiState.value = s.copy(error = "No schedule loaded")
            return
        }
        val id = loaded.id
        // Build DTO. Use ISO-8601 like we do in create flows.
        val durationMinutes = s.durationText.trim().takeIf { it.isNotBlank() }?.toIntOrNull()
        if (s.durationText.isNotBlank() && durationMinutes == null) {
            _uiState.value = s.copy(error = "Duration must be a number of minutes")
            return
        }
        val participantIds = s.participantsText
            .split(',')
            .mapNotNull { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
            .ifEmpty { null }

        val startIso = s.startTimeText.takeIf { it.isNotBlank() }?.let { combineToIso(loaded.date, it) }
        if (s.startTimeText.isNotBlank() && startIso == null) {
            _uiState.value = s.copy(error = "Start Time must be HH:mm")
            return
        }
        val endIso = s.endTimeText.takeIf { it.isNotBlank() }?.let { combineToIso(loaded.date, it) }
        if (s.endTimeText.isNotBlank() && endIso == null) {
            _uiState.value = s.copy(error = "End Time must be HH:mm")
            return
        }

        val dto = UpdateScheduleDto(
            startTime = startIso,
            endTime = endIso,
            durationMinutes = durationMinutes,
            status = s.status,
            date = startIso, // align with create: when start time changes, send date as same ISO
            // date and is_custom left unchanged when null
            participantIds = participantIds,
            notes = s.notesText
        )
        _uiState.value = s.copy(saving = true, error = null)
        viewModelScope.launch {
            val repo = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val token = createAuthRepository(context).getAccessToken()
            if (token.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(saving = false, error = "You must be logged in.")
                return@launch
            }
            val result = repo.updateSchedule(token, id, dto)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(saving = false)
                onSaved(result.getOrNull()!!)
            } else {
                _uiState.value = _uiState.value.copy(saving = false, error = result.exceptionOrNull()?.message ?: "Failed to update schedule")
            }
        }
    }
}
