package com.example.csl_kotlin_projekt.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.csl_kotlin_projekt.util.LogComposableLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = viewModel(factory = SplashViewModel.factory(LocalContext.current))
) {
    LogComposableLifecycle("SplashScreen")
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Animation variables
    val alphaAnimatable = remember { Animatable(0f) }
    val scaleAnimatable = remember { Animatable(0.5f) }
    
    // Start animations when composable is first created
    LaunchedEffect(Unit) {
        // Start auto-login check (no context needed now)
        viewModel.checkAutoLogin()

        // Start animations
        alphaAnimatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000)
        )
        scaleAnimatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000)
        )
    }
    
    // Handle navigation
    LaunchedEffect(uiState.shouldNavigateToHome, uiState.shouldNavigateToLogin) {
        when {
            uiState.shouldNavigateToHome -> {
                onNavigateToHome()
                viewModel.resetNavigationState()
            }
            uiState.shouldNavigateToLogin -> {
                onNavigateToLogin()
                viewModel.resetNavigationState()
            }
        }
    }
    
    // UI
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App name with animations
                Text(
                    text = "Laci's Smexy App",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(alphaAnimatable.value)
                        .scale(scaleAnimatable.value)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Loading indicator
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color.White,
                        strokeWidth = 4.dp
                    )
                }
                
                // Error message if any
                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error: $error",
                        color = Color.Red,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
