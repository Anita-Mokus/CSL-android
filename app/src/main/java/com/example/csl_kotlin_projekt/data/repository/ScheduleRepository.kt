package com.example.csl_kotlin_projekt.data.repository

import android.util.Log
import com.example.csl_kotlin_projekt.data.api.ScheduleApiService
import com.example.csl_kotlin_projekt.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ScheduleRepository(private val scheduleApiService: ScheduleApiService) {

    suspend fun getSchedulesByDay(date: Date?): Result<List<ScheduleResponseDto>> = withContext(Dispatchers.IO) {
        try {
            val formatted = date?.let {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                    timeZone = TimeZone.getDefault()
                }.format(it)
            }
            Log.d("ScheduleRepository", "Making get/schedule/day date=${formatted ?: "<none>"}")
            val response = scheduleApiService.getSchedulesByDay(formatted)
            Log.d("ScheduleRepository", "getSchedulesByDay code=${response.code()} size=${response.body()?.size ?: -1}")

            if (response.isSuccessful) {
                val body = response.body()
                return@withContext Result.success(body ?: emptyList())
            } else {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!err.isNullOrBlank()) {
                    Log.d("ScheduleRepository", "getSchedulesByDay errorBody=$err")
                }
                return@withContext Result.failure(Exception("Failed to fetch schedules for day: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllHabits(): Result<List<HabitResponseDto>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making get/habit...")
            val response = scheduleApiService.getAllHabits()
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

    suspend fun getHabitCategories(): Result<List<HabitCategory>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making get/habit/categories...")
            val response = scheduleApiService.getHabitCategories()
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

    suspend fun createCustomSchedule(scheduleDto: CreateCustomScheduleDto): Result<ScheduleResponseDto> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making post/schedule/custom...")
            val response = scheduleApiService.createCustomSchedule(scheduleDto)
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

    suspend fun createHabit(habit: CreateHabitDto): Result<HabitResponseDto> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making post/habit...")
            val response = scheduleApiService.createHabit(habit)
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

    suspend fun getAllSchedules(): Result<List<ScheduleResponseDto>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making get/schedule (all)...")
            val response = scheduleApiService.getAllSchedules()
            Log.d("ScheduleRepository", "getAllSchedules response code=${response.code()} message=${response.message()}")
            if (!response.isSuccessful) {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!err.isNullOrBlank()) {
                    Log.d("ScheduleRepository", "getAllSchedules errorBody=$err")
                }
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch schedules: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "getAllSchedules exception", e)
            Result.failure(e)
        }
    }

    suspend fun createRecurringSchedule(dto: CreateRecurringScheduleDto): Result<List<ScheduleResponseDto>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making post/schedule/recurring...")
            val response = scheduleApiService.createRecurringSchedule(dto)
            Log.d("ScheduleRepository", "createRecurringSchedule code=${response.code()} message=${response.message()}")
            if (!response.isSuccessful) {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!err.isNullOrBlank()) {
                    Log.d("ScheduleRepository", "createRecurringSchedule errorBody=$err")
                }
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create recurring schedules: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createWeekdayRecurringSchedule(dto: CreateWeekdayRecurringScheduleDto): Result<List<ScheduleResponseDto>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making post/schedule/recurring/weekdays...")
            val response = scheduleApiService.createWeekdayRecurringSchedule(dto)
            Log.d("ScheduleRepository", "createWeekdayRecurringSchedule code=${response.code()} message=${response.message()}")
            if (!response.isSuccessful) {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!err.isNullOrBlank()) {
                    Log.d("ScheduleRepository", "createWeekdayRecurringSchedule errorBody=$err")
                }
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create weekday recurring schedules: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProgress(dto: CreateProgressDto): Result<ProgressResponseDto> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making post/progress...")
            val response = scheduleApiService.createProgress(dto)
            Log.d("ScheduleRepository", "createProgress response code=${response.code()} message=${response.message()}")
            if (!response.isSuccessful) {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!err.isNullOrBlank()) {
                    Log.d("ScheduleRepository", "createProgress errorBody=$err")
                }
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create progress: ${response.message()}") )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getScheduleById(id: Int): Result<ScheduleResponseDto> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making get/schedule/$id...")
            val response = scheduleApiService.getScheduleById(id)
            Log.d("ScheduleRepository", "getScheduleById response code=${response.code()} message=${response.message()}")
            if (!response.isSuccessful) {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!err.isNullOrBlank()) {
                    Log.d("ScheduleRepository", "getScheduleById errorBody=$err")
                }
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch schedule: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSchedule(id: Int, dto: UpdateScheduleDto): Result<ScheduleResponseDto> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making patch/schedule/$id... body=$dto")
            val response = scheduleApiService.updateSchedule(id, dto)
            Log.d("ScheduleRepository", "updateSchedule response code=${response.code()} message=${response.message()}")
            if (!response.isSuccessful) {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!err.isNullOrBlank()) {
                    Log.d("ScheduleRepository", "updateSchedule errorBody=$err")
                }
                val msg = buildString {
                    append("Failed to update schedule: ")
                    append(response.code())
                    response.message().takeIf { it.isNotBlank() }?.let { append(" ").append(it) }
                    if (!err.isNullOrBlank()) append(" - ").append(err)
                }
                return@withContext Result.failure(Exception(msg))
            }
            if (response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update schedule: empty body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSchedule(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making delete/schedule/$id...")
            val response = scheduleApiService.deleteSchedule(id)
            Log.d("ScheduleRepository", "deleteSchedule response code=${response.code()} message=${response.message()}")
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!err.isNullOrBlank()) Log.d("ScheduleRepository", "deleteSchedule errorBody=$err")
                val msg = buildString {
                    append("Failed to delete schedule: ")
                    append(response.code())
                    response.message().takeIf { it.isNotBlank() }?.let { append(" ").append(it) }
                    if (!err.isNullOrBlank()) append(" - ").append(err)
                }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHabitsByUser(userId: Int): Result<List<HabitResponseDto>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making get/habit/user/$userId...")
            val response = scheduleApiService.getHabitsByUser(userId)
            Log.d("ScheduleRepository", "getHabitsByUser response code=${response.code()} message=${response.message()}")
            if (!response.isSuccessful) {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!err.isNullOrBlank()) Log.d("ScheduleRepository", "getHabitsByUser errorBody=$err")
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch user's habits: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
