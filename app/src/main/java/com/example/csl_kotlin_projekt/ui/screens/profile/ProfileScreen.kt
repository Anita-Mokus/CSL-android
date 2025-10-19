package com.example.csl_kotlin_projekt.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddHabit: () -> Unit,
    onLoggedOut: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load(context)
    }

    // Handle logout success
    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) onLoggedOut()
    }

    // Logout confirmation dialog
    if (uiState.showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelLogout() },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmLogout(context) }, enabled = !uiState.loggingOut) {
                    Text(if (uiState.loggingOut) "Logging out..." else "Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelLogout() }, enabled = !uiState.loggingOut) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEditProfile) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Profile")
                    }
                    IconButton(onClick = { viewModel.openLogoutConfirm() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                uiState.isLoading -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {
                    val p = uiState.profile
                    if (p != null) {
                        Text(text = p.username, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(text = p.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!p.description.isNullOrBlank()) {
                            Text(text = p.description ?: "", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = onNavigateToAddHabit) { Text("Add Habit") }
                            OutlinedButton(onClick = onNavigateToEditProfile) { Text("Edit Profile") }
                            OutlinedButton(onClick = { viewModel.openLogoutConfirm() }) { Text("Logout") }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(text = "My Habits", style = MaterialTheme.typography.titleMedium)
                        if (uiState.habits.isEmpty()) {
                            Text(text = "No habits yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(uiState.habits) { h ->
                                    // Find matching progress summary for this habit (if any)
                                    val hp = uiState.habitProgress.find { it.habit.id == h.id }

                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(text = h.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            if (!h.description.isNullOrBlank()) {
                                                Text(text = h.description ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text(text = "Category: ${h.category.name}", style = MaterialTheme.typography.bodySmall)
                                            Text(text = "Goal: ${h.goal}", style = MaterialTheme.typography.bodySmall)

                                            // Show progress line if we have a summary
                                            if (hp != null) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                val percent = hp.percent.coerceIn(0f, 1f)
                                                LinearProgressIndicator(progress = { percent }, modifier = Modifier.fillMaxWidth())
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                    Text(text = "${(percent * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                                                    Text(text = "${hp.completedSchedules}/${hp.totalSchedules} completed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text(text = "${hp.totalLoggedMinutes}m logged", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
