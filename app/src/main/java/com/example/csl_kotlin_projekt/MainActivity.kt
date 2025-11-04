package com.example.csl_kotlin_projekt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.csl_kotlin_projekt.ui.navigation.AppNavigation
import com.example.csl_kotlin_projekt.ui.theme.CSLKotlinProjektTheme
import com.example.csl_kotlin_projekt.util.AppLog

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        android.util.Log.i("AL/MainActivity", "========== MAINACTIVITY ONCREATE ==========")
        AppLog.i("MainActivity", "onCreate")
        super.onCreate(savedInstanceState)
        android.util.Log.i("AL/MainActivity", "About to call enableEdgeToEdge")
        enableEdgeToEdge()
        android.util.Log.i("AL/MainActivity", "About to setContent")
        setContent {
            CSLKotlinProjektTheme {
                AppNavigation()
            }
        }
        android.util.Log.i("AL/MainActivity", "onCreate finished")
    }

    override fun onStart() {
        super.onStart()
        AppLog.i("MainActivity", "onStart")
    }

    override fun onResume() {
        super.onResume()
        AppLog.i("AL/MainActivity", "onResume")
    }

    override fun onPause() {
        AppLog.i("AL/MainActivity", "onPause")
        super.onPause()
    }

    override fun onStop() {
        AppLog.i("AL/MainActivity", "onStop")
        super.onStop()
    }

    override fun onDestroy() {
        AppLog.i("AL/MainActivity", "onDestroy")
        super.onDestroy()
    }
}