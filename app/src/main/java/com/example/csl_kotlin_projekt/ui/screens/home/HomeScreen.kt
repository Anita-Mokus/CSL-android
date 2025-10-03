package com.example.csl_kotlin_projekt.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Load user info when screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadUserInfo(context)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laci's Smexy App") },
                actions = {
                    IconButton(onClick = { viewModel.logout(context, onNavigateToLogin) }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Welcome Message
                Text(
                    text = "Welcome to Home Screen!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // User Info
                if (uiState.username != null) {
                    Text(
                        text = "Hello, ${uiState.username}!",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (uiState.email != null) {
                        Text(
                            text = uiState.email!!,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Description
                Text(
                    text = "This is the main home screen where the user's daily schedule will be displayed",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Logout Error
                if (uiState.logoutError != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.logoutError!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Logout Button
                Button(
                    onClick = { viewModel.logout(context, onNavigateToLogin) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (uiState.isLoading) "Logging Out..." else "Logout")
                }
            }
        }
    }
}
