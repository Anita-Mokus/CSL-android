package com.example.csl_kotlin_projekt.ui.screens.editschedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.csl_kotlin_projekt.data.models.ScheduleStatus
import com.example.csl_kotlin_projekt.util.LogComposableLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleScreen(
    scheduleId: Int,
    onNavigateBack: () -> Unit,
    onSaved: (updatedId: Int) -> Unit,
    viewModel: EditScheduleViewModel = viewModel()
) {
    LogComposableLifecycle("EditScheduleScreen")
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(scheduleId) {
        viewModel.load(context, scheduleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Schedule") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            OutlinedTextField(
                value = uiState.startTimeText,
                onValueChange = viewModel::setStartTime,
                label = { Text("Start Time (HH:mm)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.endTimeText,
                onValueChange = viewModel::setEndTime,
                label = { Text("End Time (HH:mm, optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.durationText,
                onValueChange = viewModel::setDuration,
                label = { Text("Duration (minutes, optional)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Status dropdown
            var statusMenu by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = uiState.status.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Status") },
                trailingIcon = {
                    IconButton(onClick = { statusMenu = true }) {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select status")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(expanded = statusMenu, onDismissRequest = { statusMenu = false }) {
                listOf(ScheduleStatus.Planned, ScheduleStatus.Completed, ScheduleStatus.Skipped).forEach { st ->
                    DropdownMenuItem(text = { Text(st.name) }, onClick = {
                        viewModel.setStatus(st)
                        statusMenu = false
                    })
                }
            }

            OutlinedTextField(
                value = uiState.participantsText,
                onValueChange = viewModel::setParticipants,
                label = { Text("Participant IDs (comma-separated)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.notesText,
                onValueChange = viewModel::setNotes,
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    viewModel.save(context) { updated ->
                        onSaved(updated.id)
                    }
                }, enabled = !uiState.saving) {
                    Text(if (uiState.saving) "Saving..." else "Save")
                }
                TextButton(onClick = onNavigateBack, enabled = !uiState.saving) {
                    Text("Cancel")
                }
            }
        }
    }
}
