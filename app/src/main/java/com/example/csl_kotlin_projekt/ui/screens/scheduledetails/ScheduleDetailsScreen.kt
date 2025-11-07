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
import androidx.compose.material3.Button
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.csl_kotlin_projekt.util.LogComposableLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailsScreen(
    scheduleId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit = {},
    viewModel: ScheduleDetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = ScheduleDetailsViewModel.factory(LocalContext.current))
) {
    LogComposableLifecycle("ScheduleDetailsScreen")
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(scheduleId) {
        viewModel.load(scheduleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(scheduleId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    var menuOpen = remember { mutableStateOf(false) }
                    IconButton(onClick = { menuOpen.value = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = menuOpen.value, onDismissRequest = { menuOpen.value = false }) {
                        DropdownMenuItem(text = { Text("Delete") }, onClick = {
                            menuOpen.value = false
                            viewModel.openDeleteConfirm()
                        })
                    }
                }
            )
        }
    ) { paddingValues ->
        // Navigate back when deleted
        LaunchedEffect(uiState.deleted) {
            if (uiState.deleted) onNavigateBack()
        }

        // Delete confirmation dialog
        if (uiState.showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelDelete() },
                title = { Text("Delete schedule") },
                text = { Text("Are you sure?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmDelete() }, enabled = !uiState.deleting) {
                        Text(if (uiState.deleting) "Deleting..." else "Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDelete() }, enabled = !uiState.deleting) { Text("Cancel") }
                }
            )
        }

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

                    // Schedule notes with inline edit
                    Text(text = "Notes", style = MaterialTheme.typography.titleMedium)
                    if (uiState.editingNotes) {
                        OutlinedTextField(
                            value = uiState.notesDraft,
                            onValueChange = viewModel::setNotesDraft,
                            label = { Text("Edit notes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { viewModel.saveNotes() }, enabled = !uiState.savingNotes) {
                                Text(if (uiState.savingNotes) "Saving..." else "Save")
                            }
                            TextButton(onClick = { viewModel.cancelEditNotes() }, enabled = !uiState.savingNotes) {
                                Text("Cancel")
                            }
                        }
                    } else {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = s.notes?.takeIf { it.isNotBlank() } ?: "No notes yet.",
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row {
                            TextButton(onClick = { viewModel.startEditNotes() }) { Text("Edit Notes") }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

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
