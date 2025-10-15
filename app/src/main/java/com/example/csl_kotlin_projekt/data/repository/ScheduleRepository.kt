package com.example.csl_kotlin_projekt.data.repository

import android.util.Log
import com.example.csl_kotlin_projekt.data.api.ScheduleApiService
import com.example.csl_kotlin_projekt.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ScheduleRepository(private val scheduleApiService: ScheduleApiService) {

    suspend fun getSchedulesByDay(date: Date?, accessToken: String): Result<List<ScheduleResponseDto>> = withContext(Dispatchers.IO) {
        try {
            // Attempt 1: yyyy-MM-dd (common simple date)
            val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val d1 = date?.let { dayFmt.format(it) }
            Log.d("ScheduleRepository", "Making get/schedule/day... attempt1 date=${d1 ?: "<none>"}")
            val r1 = scheduleApiService.getSchedulesByDay("Bearer $accessToken", d1)
            Log.d("ScheduleRepository", "getSchedulesByDay attempt1 code=${r1.code()} size=${r1.body()?.size ?: -1}")
            if (r1.isSuccessful && !r1.body().isNullOrEmpty()) return@withContext Result.success(r1.body()!!)

            // Attempt 2: ISO start-of-day in local TZ
            if (date != null) {
                val localStart = java.util.Calendar.getInstance().apply {
                    time = date
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.time
                val isoLocal = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()).apply {
                    timeZone = TimeZone.getDefault()
                }.format(localStart)
                Log.d("ScheduleRepository", "Making get/schedule/day... attempt2 date=$isoLocal")
                val r2 = scheduleApiService.getSchedulesByDay("Bearer $accessToken", isoLocal)
                Log.d("ScheduleRepository", "getSchedulesByDay attempt2 code=${r2.code()} size=${r2.body()?.size ?: -1}")
                if (r2.isSuccessful && !r2.body().isNullOrEmpty()) return@withContext Result.success(r2.body()!!)

                // Attempt 3: ISO start-of-day in UTC
                val calUtc = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    time = date
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                val isoUtc = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(calUtc.time)
                Log.d("ScheduleRepository", "Making get/schedule/day... attempt3 date=$isoUtc")
                val r3 = scheduleApiService.getSchedulesByDay("Bearer $accessToken", isoUtc)
                Log.d("ScheduleRepository", "getSchedulesByDay attempt3 code=${r3.code()} size=${r3.body()?.size ?: -1}")
                if (r3.isSuccessful && !r3.body().isNullOrEmpty()) return@withContext Result.success(r3.body()!!)
            }

            // Attempt 4: No date parameter - let server default to today
            Log.d("ScheduleRepository", "Making get/schedule/day... attempt4 date=<none>")
            val r4 = scheduleApiService.getSchedulesByDay("Bearer $accessToken", null)
            Log.d("ScheduleRepository", "getSchedulesByDay attempt4 code=${r4.code()} size=${r4.body()?.size ?: -1}")
            if (r4.isSuccessful && r4.body() != null) {
                return@withContext Result.success(r4.body()!!)
            }

            // If none returned results, return the last failure or empty-success
            val last = listOfNotNull(
                r1.takeIf { !it.isSuccessful },
                r1.takeIf { it.isSuccessful && it.body().isNullOrEmpty() },
            ).lastOrNull()
            if (last != null) {
                Result.failure(Exception("Failed to fetch schedule: ${last.message()}"))
            } else {
                Result.success(emptyList())
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

    suspend fun getAllSchedules(accessToken: String): Result<List<ScheduleResponseDto>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making get/schedule (all)...")
            val response = scheduleApiService.getAllSchedules("Bearer $accessToken")
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

    suspend fun createRecurringSchedule(accessToken: String, dto: CreateRecurringScheduleDto): Result<List<ScheduleResponseDto>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making post/schedule/recurring...")
            val response = scheduleApiService.createRecurringSchedule("Bearer $accessToken", dto)
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

    suspend fun createWeekdayRecurringSchedule(accessToken: String, dto: CreateWeekdayRecurringScheduleDto): Result<List<ScheduleResponseDto>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making post/schedule/recurring/weekdays...")
            val response = scheduleApiService.createWeekdayRecurringSchedule("Bearer $accessToken", dto)
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

    suspend fun createProgress(accessToken: String, dto: CreateProgressDto): Result<ProgressResponseDto> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making post/progress...")
            val response = scheduleApiService.createProgress("Bearer $accessToken", dto)
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
                Result.failure(Exception("Failed to create progress: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getScheduleById(accessToken: String, id: Int): Result<ScheduleResponseDto> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making get/schedule/$id...")
            val response = scheduleApiService.getScheduleById("Bearer $accessToken", id)
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

    suspend fun updateSchedule(accessToken: String, id: Int, dto: UpdateScheduleDto): Result<ScheduleResponseDto> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making patch/schedule/$id... body=$dto")
            val response = scheduleApiService.updateSchedule("Bearer $accessToken", id, dto)
            Log.d("ScheduleRepository", "updateSchedule response code=${response.code()} message=${response.message()}")
            if (!response.isSuccessful) {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!err.isNullOrBlank()) {
                    Log.d("ScheduleRepository", "updateSchedule errorBody=$err")
                }
                val msg = buildString {
                    append("Failed to update schedule: ")
                    append(response.code())
                    response.message()?.takeIf { it.isNotBlank() }?.let { append(" ").append(it) }
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

    suspend fun deleteSchedule(accessToken: String, id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("ScheduleRepository", "Making delete/schedule/$id...")
            val response = scheduleApiService.deleteSchedule("Bearer $accessToken", id)
            Log.d("ScheduleRepository", "deleteSchedule response code=${response.code()} message=${response.message()}")
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                if (!err.isNullOrBlank()) Log.d("ScheduleRepository", "deleteSchedule errorBody=$err")
                val msg = buildString {
                    append("Failed to delete schedule: ")
                    append(response.code())
                    response.message()?.takeIf { it.isNotBlank() }?.let { append(" ").append(it) }
                    if (!err.isNullOrBlank()) append(" - ").append(err)
                }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
