package com.example.csl_kotlin_projekt.data.repository

import android.util.Log
import com.example.csl_kotlin_projekt.data.api.ScheduleApiService
import com.example.csl_kotlin_projekt.data.models.ScheduleResponseDto
import com.example.csl_kotlin_projekt.data.models.HabitResponseDto
import com.example.csl_kotlin_projekt.data.models.CreateCustomScheduleDto
import com.example.csl_kotlin_projekt.data.models.CreateHabitDto
import com.example.csl_kotlin_projekt.data.models.HabitCategory
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

    suspend fun getAllHabits(accessToken: String): Result<List<HabitResponseDto>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making get/habit...")
            val response = scheduleApiService.getAllHabits("Bearer $accessToken")
            Log.d("ScheduleRepository", "getAllHabits response code=${response.code()} message=${response.message()}")
            if (!response.isSuccessful) {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!err.isNullOrBlank()) {
                    Log.d("ScheduleRepository", "getAllHabits errorBody=$err")
                }
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch habits: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "getAllHabits exception", e)
            Result.failure(e)
        }
    }

    suspend fun getHabitCategories(accessToken: String): Result<List<HabitCategory>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making get/habit/categories...")
            val response = scheduleApiService.getHabitCategories("Bearer $accessToken")
            Log.d("ScheduleRepository", "getHabitCategories response code=${response.code()} message=${response.message()}")
            if (!response.isSuccessful) {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!err.isNullOrBlank()) {
                    Log.d("ScheduleRepository", "getHabitCategories errorBody=$err")
                }
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch habit categories: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "getHabitCategories exception", e)
            Result.failure(e)
        }
    }

    suspend fun createCustomSchedule(accessToken: String, scheduleDto: CreateCustomScheduleDto): Result<ScheduleResponseDto> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making post/schedule/custom...")
            val response = scheduleApiService.createCustomSchedule("Bearer $accessToken", scheduleDto)
            Log.d("ScheduleRepository", "createCustomSchedule response code=${response.code()} message=${response.message()}")
            if (!response.isSuccessful) {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!err.isNullOrBlank()) {
                    Log.d("ScheduleRepository", "createCustomSchedule errorBody=$err")
                }
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create schedule: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createHabit(accessToken: String, habit: CreateHabitDto): Result<HabitResponseDto> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making post/habit...")
            val response = scheduleApiService.createHabit("Bearer $accessToken", habit)
            Log.d("ScheduleRepository", "createHabit response code=${response.code()} message=${response.message()}")
            if (!response.isSuccessful) {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!err.isNullOrBlank()) {
                    Log.d("ScheduleRepository", "createHabit errorBody=$err")
                }
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create habit: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "createHabit exception", e)
            Result.failure(e)
        }
    }
}
