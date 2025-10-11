package com.example.csl_kotlin_projekt.ui.screens.addhabit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csl_kotlin_projekt.data.models.CreateHabitDto
import com.example.csl_kotlin_projekt.data.models.HabitCategory
import com.example.csl_kotlin_projekt.data.network.NetworkModule
import com.example.csl_kotlin_projekt.data.repository.ScheduleRepository
import com.example.csl_kotlin_projekt.data.repository.createAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddHabitUiState(
    val name: String = "",
    val description: String = "",
    val categoryId: String = "",
    val goal: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreated: Boolean = false,
    val categories: List<HabitCategory> = emptyList(),
    val selectedCategory: HabitCategory? = null,
)

class AddHabitViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AddHabitUiState())
    val uiState: StateFlow<AddHabitUiState> = _uiState.asStateFlow()

    fun updateName(v: String) { _uiState.value = _uiState.value.copy(name = v, error = null) }
    fun updateDescription(v: String) { _uiState.value = _uiState.value.copy(description = v, error = null) }
    fun updateCategoryId(v: String) { _uiState.value = _uiState.value.copy(categoryId = v, error = null) }
    fun updateGoal(v: String) { _uiState.value = _uiState.value.copy(goal = v, error = null) }

    fun onCategorySelected(cat: HabitCategory) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = cat,
            categoryId = cat.id.toString(), // keep numeric field in sync
            error = null
        )
    }

    fun loadCategories(context: android.content.Context) {
        Log.d("AddHabitVM", "loadCategories called")
        viewModelScope.launch {
            val authRepo = createAuthRepository(context)
            val token = authRepo.getAccessToken()
            if (token.isNullOrBlank()) {
                Log.w("AddHabitVM", "No access token found; cannot load categories")
                // Don't block creation UI; just show error message
                _uiState.value = _uiState.value.copy(error = "You must be logged in to load categories.")
                return@launch
            }
            val repo = ScheduleRepository(NetworkModule.createScheduleApiService(context))
            val result = repo.getHabitCategories(token)
            if (result.isSuccess) {
                val list = result.getOrNull().orEmpty()
                Log.d("AddHabitVM", "getHabitCategories succeeded: count=${list.size}")
                _uiState.value = _uiState.value.copy(categories = list, error = null)
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Failed to load categories"
                Log.w("AddHabitVM", "getHabitCategories failed: $msg")
                _uiState.value = _uiState.value.copy(error = msg)
            }
        }
    }

    fun createHabit(context: android.content.Context) {
        val s = _uiState.value
        if (s.name.isBlank()) { _uiState.value = s.copy(error = "Name is required"); return }
        val catId = s.categoryId.toIntOrNull()
        if (catId == null || catId <= 0) { _uiState.value = s.copy(error = "Valid categoryId is required"); return }
        if (s.goal.isBlank()) { _uiState.value = s.copy(error = "Goal is required"); return }

        _uiState.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val authRepo = createAuthRepository(context)
                val token = authRepo.getAccessToken()
                if (token.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "You must be logged in to create a habit.")
                    return@launch
                }
                val repo = ScheduleRepository(NetworkModule.createScheduleApiService(context))
                val dto = CreateHabitDto(
                    name = s.name,
                    description = s.description.ifBlank { null },
                    categoryId = catId,
                    goal = s.goal
                )
                Log.d("AddHabitVM", "Calling createHabit")
                val result = repo.createHabit(token, dto)
                if (result.isSuccess) {
                    Log.d("AddHabitVM", "createHabit success")
                    _uiState.value = _uiState.value.copy(isLoading = false, isCreated = true)
                } else {
                    val msg = result.exceptionOrNull()?.message ?: "Failed to create habit"
                    Log.w("AddHabitVM", "createHabit failed: $msg")
                    _uiState.value = _uiState.value.copy(isLoading = false, error = msg)
                }
            } catch (e: Exception) {
                Log.e("AddHabitVM", "createHabit exception", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Unexpected error")
            }
        }
    }
}
