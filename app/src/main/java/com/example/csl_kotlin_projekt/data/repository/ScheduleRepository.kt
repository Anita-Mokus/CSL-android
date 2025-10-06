package com.example.csl_kotlin_projekt.data.repository

import com.example.csl_kotlin_projekt.data.api.ScheduleApiService
import com.example.csl_kotlin_projekt.data.models.ScheduleResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduleRepository(private val scheduleApiService: ScheduleApiService) {

    suspend fun getSchedulesByDay(date: Date?): Result<List<ScheduleResponseDto>> = withContext(Dispatchers.IO) {
        try {
            val dateString = date?.let {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
            }
            val response = scheduleApiService.getSchedulesByDay(dateString)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch schedule: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
