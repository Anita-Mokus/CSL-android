package com.example.csl_kotlin_projekt.ui.screens.createschedule

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateScheduleViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHabits(context)
    }

    LaunchedEffect(uiState.isScheduleCreated) {
        if (uiState.isScheduleCreated) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Schedule") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Habit Selector
            HabitSelector(
                habits = uiState.habits,
                selectedHabit = uiState.selectedHabit,
                onHabitSelected = viewModel::onHabitSelected
            )

            // Time Picker
            TimePicker(
                calendar = uiState.startTime,
                onTimeSelected = viewModel::onTimeSelected
            )

            // Duration Input
            OutlinedTextField(
                value = uiState.durationMinutes,
                onValueChange = viewModel::onDurationChanged,
                label = { Text("Duration (minutes, optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Notes Input
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChanged,
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Error Message
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Create Button
            Button(
                onClick = { viewModel.createSchedule(context) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Create Schedule")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitSelector(
    habits: List<com.example.csl_kotlin_projekt.data.models.HabitResponseDto>,
    selectedHabit: com.example.csl_kotlin_projekt.data.models.HabitResponseDto?,
    onHabitSelected: (com.example.csl_kotlin_projekt.data.models.HabitResponseDto) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedHabit?.name ?: "Select a habit",
            onValueChange = {},
            readOnly = true,
            label = { Text("Habit") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            habits.forEach { habit ->
                DropdownMenuItem(
                    text = { Text(habit.name) },
                    onClick = {
                        onHabitSelected(habit)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TimePicker(
    calendar: Calendar,
    onTimeSelected: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute -> onTimeSelected(hourOfDay, minute) },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    OutlinedTextField(
        value = timeFormat.format(calendar.time),
        onValueChange = {},
        readOnly = true,
        label = { Text("Start Time") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { timePickerDialog.show() }
    )
}
