package com.example.csl_kotlin_projekt.data.api

import com.example.csl_kotlin_projekt.data.models.ScheduleResponseDto
import com.example.csl_kotlin_projekt.data.models.HabitResponseDto
import com.example.csl_kotlin_projekt.data.models.CreateCustomScheduleDto
import com.example.csl_kotlin_projekt.data.models.CreateHabitDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ScheduleApiService {
    @GET("schedule/day")
    suspend fun getSchedulesByDay(
        @Query("date") date: String?
    ): Response<List<ScheduleResponseDto>>

    @GET("habit")
    suspend fun getAllHabits(
        @Header("Authorization") authorization: String
    ): Response<List<HabitResponseDto>>

    @POST("schedule/custom")
    suspend fun createCustomSchedule(
        @Body schedule: CreateCustomScheduleDto
    ): Response<ScheduleResponseDto>

    @POST("habit")
    suspend fun createHabit(
        @Header("Authorization") authorization: String,
        @Body habit: CreateHabitDto
    ): Response<HabitResponseDto>
}
