package com.example.csl_kotlin_projekt.ui.screens.scheduledetails

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailsScreen(
    scheduleId: Int,
    onNavigateBack: () -> Unit,
    viewModel: ScheduleDetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(scheduleId) {
        viewModel.load(context, scheduleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
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
                uiState.schedule != null -> {
                    val s = uiState.schedule
                    Text(text = s!!.habit.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    if (!s.habit.description.isNullOrBlank()) {
                        Text(text = s.habit.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(12.dp))

                    // Progress bar
                    val totalLogged = s.progress?.sumOf { it.loggedTime ?: 0 } ?: 0
                    val goal = s.durationMinutes
                    val progress = when {
                        goal != null && goal > 0 -> (totalLogged.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
                        else -> if (s.progress?.maxByOrNull { it.createdAt }?.isCompleted == true) 1f else 0f
                    }
                    Text(text = "Completion", style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp))
                    val label = if (goal != null) "${totalLogged} / ${goal} min" else if (progress >= 1f) "Completed" else "Not completed"
                    Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(16.dp))

                    // Schedule notes
                    if (!s.notes.isNullOrBlank()) {
                        Text(text = "Notes", style = MaterialTheme.typography.titleMedium)
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(text = s.notes, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // Recent activities (progress history)
                    Text(text = "Recent Activity", style = MaterialTheme.typography.titleMedium)
                    val itemsList = (s.progress ?: emptyList()).sortedByDescending { it.date }
                    if (itemsList.isEmpty()) {
                        Text(text = "No activity yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(itemsList) { p ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(p.date)
                                        Text(text = dateStr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        val details = buildString {
                                            if (p.loggedTime != null) append("Logged: ${p.loggedTime} min\n")
                                            append("Completed: ${if (p.isCompleted) "Yes" else "No"}")
                                        }
                                        Text(text = details)
                                        if (!p.notes.isNullOrBlank()) {
                                            Spacer(Modifier.height(6.dp))
                                            Text(text = p.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
