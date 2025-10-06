package com.example.csl_kotlin_projekt.data.models

import com.google.gson.annotations.SerializedName
import java.util.Date

enum class ScheduleStatus {
    Planned,
    Completed,
    Skipped
}

data class ProgressResponseDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("scheduleId")
    val scheduleId: Int,
    @SerializedName("date")
    val date: Date,
    @SerializedName("logged_time")
    val loggedTime: Int?,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("is_completed")
    val isCompleted: Boolean,
    @SerializedName("created_at")
    val createdAt: Date,
    @SerializedName("updated_at")
    val updatedAt: Date
)

data class HabitResponseDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("goal")
    val goal: String,
    @SerializedName("category")
    val category: HabitCategory,
    @SerializedName("created_at")
    val createdAt: Date,
    @SerializedName("updated_at")
    val updatedAt: Date
)

data class HabitCategoryResponseDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("iconUrl")
    val iconUrl: String?
)

data class ScheduleResponseDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("start_time")
    val startTime: Date,
    @SerializedName("end_time")
    val endTime: Date?,
    @SerializedName("status")
    val status: ScheduleStatus,
    @SerializedName("date")
    val date: Date,
    @SerializedName("is_custom")
    val isCustom: Boolean,
    @SerializedName("created_at")
    val createdAt: Date,
    @SerializedName("updated_at")
    val updatedAt: Date,
    @SerializedName("habit")
    val habit: HabitResponseDto,
    @SerializedName("progress")
    val progress: List<ProgressResponseDto>?,
    @SerializedName("participants")
    val participants: List<UserDto>?,
    @SerializedName("type")
    val type: String,
    @SerializedName("duration_minutes")
    val durationMinutes: Int?,
    @SerializedName("is_participant_only")
    val isParticipantOnly: Boolean,
    @SerializedName("notes")
    val notes: String?
)
