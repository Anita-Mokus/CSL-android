package com.example.csl_kotlin_projekt.ui.screens.home

import HomeViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.util.Base64
import android.app.DatePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToAddSchedule: () -> Unit,
    onNavigateToAddHabit: () -> Unit = {},
    onNavigateToScheduleDetails: (Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    // Selected day for filtering (defaults to today)
    var selectedDate by remember { mutableStateOf(Date()) }
    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Load user info and schedule when screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadUserInfo(context)
        viewModel.loadSchedule(context, selectedDate)
    }

    // Reload schedule whenever the screen resumes (e.g., after creating a schedule)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, selectedDate) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadSchedule(context, selectedDate)
                viewModel.loadUserInfo(context) // refresh profile image if changed
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Handle successful logout
    LaunchedEffect(uiState.isLogoutSuccessful) {
        if (uiState.isLogoutSuccessful) {
            onNavigateToLogin()
        }
    }

    // Prepare model for profile image (url or decoded base64)
    val profileImageModel by remember(uiState.profileImageUrl, uiState.profileImageBase64) {
        mutableStateOf(
            when {
                !uiState.profileImageUrl.isNullOrBlank() -> uiState.profileImageUrl
                !uiState.profileImageBase64.isNullOrBlank() -> try {
                    val raw = uiState.profileImageBase64
                    val cleaned = raw?.substringAfter("base64,", missingDelimiterValue = raw)
                    Base64.decode(cleaned, Base64.DEFAULT)
                } catch (_: Exception) { null }
                else -> null
            }
        )
    }

    // Progress dialog
    if (uiState.showProgressDialog) {
        AlertDialog(
            onDismissRequest = { if (!uiState.progressSubmitting) viewModel.closeProgressDialog() },
            title = { Text("Add Progress") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.progressLoggedTime,
                        onValueChange = viewModel::updateProgressLoggedTime,
                        label = { Text("Logged time (minutes, optional)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.progressNotes,
                        onValueChange = viewModel::updateProgressNotes,
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = uiState.progressCompleted, onCheckedChange = { viewModel.toggleProgressCompleted() })
                        Spacer(Modifier.width(8.dp))
                        Text("Mark as completed")
                    }
                    if (uiState.progressError != null) {
                        Text(uiState.progressError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.submitProgress(context) }, enabled = !uiState.progressSubmitting) {
                    if (uiState.progressSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeProgressDialog() }, enabled = !uiState.progressSubmitting) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laci's Smexy App") },
                actions = {
                    IconButton(onClick = { onNavigateToProfile() }) {
                        if (profileImageModel != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(profileImageModel)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile",
                                modifier = Modifier.size(32.dp).clip(CircleShape)
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = "Profile")
                        }
                    }
                    // Overflow menu for extra actions
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Add Habit") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToAddHabit()
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAddSchedule() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header + date filter controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Schedule",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date selector + Filter button
            val cal = remember(selectedDate) {
                Calendar.getInstance().apply { time = selectedDate }
            }
            val datePickerDialog = remember(selectedDate) {
                DatePickerDialog(
                    context,
                    { _, y, m, d ->
                        val c = Calendar.getInstance().apply {
                            set(Calendar.YEAR, y)
                            set(Calendar.MONTH, m)
                            set(Calendar.DAY_OF_MONTH, d)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        selectedDate = c.time
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = dateFmt.format(selectedDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filter day") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { datePickerDialog.show() }
                    )
                }
                Button(onClick = { viewModel.loadSchedule(context, selectedDate) }, enabled = !uiState.isLoading) {
                    Text("Filter")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Loading Indicator
            if (uiState.isLoading) {
                CircularProgressIndicator()
            }

            // Error Message
            if (uiState.scheduleError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.scheduleError!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            // Empty Schedule Message
            if (!uiState.isLoading && uiState.schedule.isEmpty() && uiState.scheduleError == null) {
                Text(
                    text = "No schedules for ${dateFmt.format(selectedDate)}.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Schedule List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.schedule) { schedule ->
                    ScheduleItem(
                        schedule = schedule,
                        onLogProgress = { viewModel.openProgressDialog(schedule.id) },
                        onToggleCompleted = { checked -> viewModel.toggleScheduleCompleted(context, schedule.id, checked) },
                        isToggling = uiState.togglingScheduleIds.contains(schedule.id),
                        desiredCompleted = uiState.togglingDesired[schedule.id],
                        onOpenDetails = { onNavigateToScheduleDetails(schedule.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleItem(
    schedule: com.example.csl_kotlin_projekt.data.models.ScheduleResponseDto,
    onLogProgress: () -> Unit,
    onToggleCompleted: (Boolean) -> Unit,
    isToggling: Boolean,
    desiredCompleted: Boolean?,
    onOpenDetails: () -> Unit
) {
    val totalLogged = remember(schedule.progress) {
        schedule.progress?.sumOf { it.loggedTime ?: 0 } ?: 0
    }
    val goalMinutes = schedule.durationMinutes

    // Determine completion based on latest progress entry if present; otherwise fall back to server status
    val latestProgressCompleted = remember(schedule.progress) {
        schedule.progress?.maxByOrNull { it.createdAt }?.isCompleted
    }
    val derivedCompleted = latestProgressCompleted ?: (schedule.status == com.example.csl_kotlin_projekt.data.models.ScheduleStatus.Completed)
    val effectiveCompleted = desiredCompleted ?: derivedCompleted

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Indicator
            Checkbox(
                checked = effectiveCompleted,
                onCheckedChange = { onToggleCompleted(it) },
                enabled = !isToggling,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Schedule Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(schedule.startTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Progress display
                val progressText = if (goalMinutes != null) {
                    "Progress: $totalLogged / $goalMinutes min"
                } else {
                    "Progress: $totalLogged min"
                }
                Text(
                    text = progressText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onLogProgress) { Text("Log") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onOpenDetails) { Text("Details") }
            }
        }
    }
}
