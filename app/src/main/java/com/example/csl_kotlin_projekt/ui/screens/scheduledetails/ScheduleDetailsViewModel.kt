package com.example.csl_kotlin_projekt.ui.screens.scheduledetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.models.ScheduleResponseDto
import com.example.csl_kotlin_projekt.data.models.UpdateScheduleDto
import com.example.csl_kotlin_projekt.data.network.NetworkModule
import com.example.csl_kotlin_projekt.data.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScheduleDetailsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val schedule: ScheduleResponseDto? = null,
    val editingNotes: Boolean = false,
    val notesDraft: String = "",
    val savingNotes: Boolean = false,
    // Delete flow
    val showDeleteConfirm: Boolean = false,
    val deleting: Boolean = false,
    val deleted: Boolean = false
)

class ScheduleDetailsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScheduleDetailsUiState())
    val uiState: StateFlow<ScheduleDetailsUiState> = _uiState.asStateFlow()

    fun load(context: android.content.Context, id: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val repo = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val result = repo.getScheduleById(id)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, schedule = result.getOrNull())
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to load schedule")
            }
        }
    }

    fun startEditNotes() {
        val s = _uiState.value.schedule
        _uiState.value = _uiState.value.copy(
            editingNotes = true,
            notesDraft = s?.notes ?: "",
            error = null
        )
    }

    fun setNotesDraft(v: String) {
        _uiState.value = _uiState.value.copy(notesDraft = v)
    }

    fun cancelEditNotes() {
        _uiState.value = _uiState.value.copy(editingNotes = false, notesDraft = "", savingNotes = false)
    }

    fun saveNotes(context: android.content.Context) {
        val current = _uiState.value
        val sched = current.schedule ?: run {
            _uiState.value = current.copy(error = "No schedule loaded")
            return
        }
        _uiState.value = current.copy(savingNotes = true, error = null)
        viewModelScope.launch {
            val repo = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val dto = UpdateScheduleDto(notes = _uiState.value.notesDraft)
            val result = repo.updateSchedule(sched.id, dto)
            if (result.isSuccess) {
                val updated = result.getOrNull()!!
                _uiState.value = _uiState.value.copy(
                    savingNotes = false,
                    editingNotes = false,
                    schedule = updated
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    savingNotes = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to update notes"
                )
            }
        }
    }

    fun openDeleteConfirm() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true, error = null)
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false)
    }

    fun confirmDelete(context: android.content.Context) {
        val s = _uiState.value.schedule ?: run {
            _uiState.value = _uiState.value.copy(error = "No schedule loaded")
            return
        }
        _uiState.value = _uiState.value.copy(deleting = true, showDeleteConfirm = false, error = null)
        viewModelScope.launch {
            val repo = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val result = repo.deleteSchedule(s.id)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(deleting = false, deleted = true)
            } else {
                _uiState.value = _uiState.value.copy(deleting = false, error = result.exceptionOrNull()?.message ?: "Failed to delete schedule")
            }
        }
    }
}
