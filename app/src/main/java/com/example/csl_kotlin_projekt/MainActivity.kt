package com.example.csl_kotlin_projekt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.csl_kotlin_projekt.ui.navigation.AppNavigation
import com.example.csl_kotlin_projekt.ui.theme.CSLKotlinProjektTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CSLKotlinProjektTheme {
                AppNavigation()
            }
        }
    }
}