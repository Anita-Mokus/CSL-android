package com.example.csl_kotlin_projekt.data.api

import com.example.csl_kotlin_projekt.data.models.ScheduleResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ScheduleApiService {
    @GET("schedule/day")
    suspend fun getSchedulesByDay(
        @Query("date") date: String?
    ): Response<List<ScheduleResponseDto>>
}
