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
        AppLog.i("MainActivity", "onCreate")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CSLKotlinProjektTheme {
                AppNavigation()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        AppLog.i("MainActivity", "onStart")
    }

    override fun onResume() {
        super.onResume()
        AppLog.i("MainActivity", "onResume")
    }

    override fun onPause() {
        AppLog.i("MainActivity", "onPause")
        super.onPause()
    }

    override fun onStop() {
        AppLog.i("MainActivity", "onStop")
        super.onStop()
    }

    override fun onDestroy() {
        AppLog.i("MainActivity", "onDestroy")
        super.onDestroy()
    }
}