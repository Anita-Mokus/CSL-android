package com.example.csl_kotlin_projekt.data.api

import com.example.csl_kotlin_projekt.data.models.ScheduleResponseDto
import com.example.csl_kotlin_projekt.data.models.HabitResponseDto
import com.example.csl_kotlin_projekt.data.models.CreateCustomScheduleDto
import com.example.csl_kotlin_projekt.data.models.CreateHabitDto
import com.example.csl_kotlin_projekt.data.models.HabitCategory
import com.example.csl_kotlin_projekt.data.models.CreateRecurringScheduleDto
import com.example.csl_kotlin_projekt.data.models.CreateWeekdayRecurringScheduleDto
import com.example.csl_kotlin_projekt.data.models.CreateProgressDto
import com.example.csl_kotlin_projekt.data.models.ProgressResponseDto
import com.example.csl_kotlin_projekt.data.models.UpdateScheduleDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Path
import retrofit2.http.PATCH
import retrofit2.http.DELETE

interface ScheduleApiService {
    @GET("schedule/day")
    suspend fun getSchedulesByDay(
        @Header("Authorization") authorization: String,
        @Query("date") date: String?
    ): Response<List<ScheduleResponseDto>>

    @GET("schedule")
    suspend fun getAllSchedules(
        @Header("Authorization") authorization: String
    ): Response<List<ScheduleResponseDto>>

    @GET("schedule/{id}")
    suspend fun getScheduleById(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Response<ScheduleResponseDto>

    @GET("habit")
    suspend fun getAllHabits(
        @Header("Authorization") authorization: String
    ): Response<List<HabitResponseDto>>

    @GET("habit/categories")
    suspend fun getHabitCategories(
        @Header("Authorization") authorization: String
    ): Response<List<HabitCategory>>

    @POST("schedule/custom")
    suspend fun createCustomSchedule(
        @Header("Authorization") authorization: String,
        @Body schedule: CreateCustomScheduleDto
    ): Response<ScheduleResponseDto>

    @POST("schedule/recurring")
    suspend fun createRecurringSchedule(
        @Header("Authorization") authorization: String,
        @Body dto: CreateRecurringScheduleDto
    ): Response<List<ScheduleResponseDto>>

    @POST("schedule/recurring/weekdays")
    suspend fun createWeekdayRecurringSchedule(
        @Header("Authorization") authorization: String,
        @Body dto: CreateWeekdayRecurringScheduleDto
    ): Response<List<ScheduleResponseDto>>

    @POST("progress")
    suspend fun createProgress(
        @Header("Authorization") authorization: String,
        @Body dto: CreateProgressDto
    ): Response<ProgressResponseDto>

    @POST("habit")
    suspend fun createHabit(
        @Header("Authorization") authorization: String,
        @Body habit: CreateHabitDto
    ): Response<HabitResponseDto>

    @PATCH("schedule/{id}")
    suspend fun updateSchedule(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body dto: UpdateScheduleDto
    ): Response<ScheduleResponseDto>

    @DELETE("schedule/{id}")
    suspend fun deleteSchedule(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Response<Unit>

    @GET("habit/user/{userId}")
    suspend fun getHabitsByUser(
        @Header("Authorization") authorization: String,
        @Path("userId") userId: Int
    ): Response<List<HabitResponseDto>>
}
