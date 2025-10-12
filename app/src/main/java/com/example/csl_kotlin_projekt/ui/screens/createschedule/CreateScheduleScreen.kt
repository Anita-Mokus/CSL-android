package com.example.csl_kotlin_projekt.ui.screens.createschedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Habit Selector
            HabitSelector(
                habits = uiState.habits,
                selectedHabit = uiState.selectedHabit,
                onHabitSelected = viewModel::onHabitSelected
            )

            // Date Picker (select any future date)
            DatePickerField(
                calendar = uiState.startTime,
                onDateSelected = viewModel::onDateSelected
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

            // Recurring options
            Text("Recurring options", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
            var rpExpanded by remember { mutableStateOf(false) }
            val patterns = listOf("none", "daily", "weekdays", "weekends")
            ExposedDropdownMenuBox(expanded = rpExpanded, onExpandedChange = { rpExpanded = !rpExpanded }) {
                OutlinedTextField(
                    value = uiState.repeatPattern,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Repeat pattern") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rpExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = rpExpanded, onDismissRequest = { rpExpanded = false }) {
                    patterns.forEach { p ->
                        DropdownMenuItem(text = { Text(p) }, onClick = {
                            viewModel.onRepeatPatternChanged(p)
                            rpExpanded = false
                        })
                    }
                }
            }

            if (uiState.repeatPattern != "none" && uiState.repeatPattern != "weekdays") {
                OutlinedTextField(
                    value = uiState.repeatDays,
                    onValueChange = viewModel::onRepeatDaysChanged,
                    label = { Text("Repeat days (default 30)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (uiState.repeatPattern == "weekdays") {
                Text("Select weekdays", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.align(Alignment.Start))
                val days = listOf(
                    "Mon" to 1, "Tue" to 2, "Wed" to 3, "Thu" to 4, "Fri" to 5, "Sat" to 6, "Sun" to 7
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    days.forEach { (label, num) ->
                        FilterChip(
                            selected = uiState.daysOfWeek.contains(num),
                            onClick = { viewModel.onToggleDayOfWeek(num) },
                            label = { Text(label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = uiState.numberOfWeeks,
                    onValueChange = viewModel::onNumberOfWeeksChanged,
                    label = { Text("Number of weeks (default 4)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Error Message
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Create Buttons
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

            Button(
                onClick = { viewModel.createRecurringSchedule(context) },
                enabled = !uiState.isLoading && uiState.repeatPattern != "none",
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Recurring (pattern)")
            }

            Button(
                onClick = { viewModel.createWeekdayRecurringSchedule(context) },
                enabled = !uiState.isLoading && uiState.daysOfWeek.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Weekday Recurring")
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
fun DatePickerField(
    calendar: Calendar,
    onDateSelected: (Int, Int, Int) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d -> onDateSelected(y, m, d) },
        year,
        month,
        day
    ).apply {
        // Block selecting past dates
        datePicker.minDate = Calendar.getInstance().timeInMillis
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = dateFormat.format(calendar.time),
            onValueChange = {},
            readOnly = true,
            label = { Text("Date") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable { datePickerDialog.show() }
        )
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

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = timeFormat.format(calendar.time),
            onValueChange = {},
            readOnly = true,
            label = { Text("Start Time") },
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable { timePickerDialog.show() }
        )
    }
}
