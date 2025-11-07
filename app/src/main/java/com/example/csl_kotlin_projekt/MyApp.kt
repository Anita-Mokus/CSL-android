package com.example.csl_kotlin_projekt

import android.app.Application
import android.util.Log
import com.example.csl_kotlin_projekt.di.AppContainer
import com.example.csl_kotlin_projekt.di.DefaultAppContainer

class MyApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        try {
            Log.e("AL/App", "========== APPLICATION STARTING ==========")
            super.onCreate()
            // Initialize simple DI container
            container = DefaultAppContainer(this)
            Log.e("AL/App", "Application.onCreate called successfully")
            Log.e("AL/App", "========================================")
        } catch (e: Exception) {
            Log.e("AL/App", "CRASH IN APPLICATION ONCREATE: ${e.message}", e)
            throw e
        }
    }
}
