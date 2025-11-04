package com.example.csl_kotlin_projekt

import android.app.Application
import android.util.Log

class MyApp : Application() {
    override fun onCreate() {
        try {
            Log.e("AL/App", "========== APPLICATION STARTING ==========")
            super.onCreate()
            Log.e("AL/App", "Application.onCreate called successfully")
            Log.e("AL/App", "========================================")
        } catch (e: Exception) {
            Log.e("AL/App", "CRASH IN APPLICATION ONCREATE: ${e.message}", e)
            throw e
        }
    }
}

