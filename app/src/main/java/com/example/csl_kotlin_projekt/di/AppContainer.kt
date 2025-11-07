package com.example.csl_kotlin_projekt.di

import android.content.Context
import com.example.csl_kotlin_projekt.data.network.NetworkModule
import com.example.csl_kotlin_projekt.data.repository.AuthRepository
import com.example.csl_kotlin_projekt.data.repository.ScheduleRepository

interface AppContainer {
    val authRepository: AuthRepository
    val scheduleRepository: ScheduleRepository
}

class DefaultAppContainer(appContext: Context) : AppContainer {
    private val context = appContext.applicationContext

    // Singletons built once per process
    override val authRepository: AuthRepository by lazy {
        val prefs = context.getSharedPreferences(AuthRepository.PREF_NAME, Context.MODE_PRIVATE)
        val authApi = NetworkModule.createAuthApiService(context)
        AuthRepository(authApi, prefs)
    }

    override val scheduleRepository: ScheduleRepository by lazy {
        val scheduleApi = NetworkModule.createScheduleApiService(context)
        ScheduleRepository(scheduleApi)
    }
}

