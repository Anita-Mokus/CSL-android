package com.example.csl_kotlin_projekt.ui.screens.profile

import android.util.Base64
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.csl_kotlin_projekt.data.repository.createAuthRepository

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

    // Refresh when returning to this screen (e.g., after editing profile image)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.load(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                        // Replace username/email header with avatar + texts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val context = LocalContext.current
                            val token = remember(context) { createAuthRepository(context).getAccessToken() }

                            // Build model like Home: prefer URL (with cache-busting), else decode base64 to bytes
                            val profileImageModel = remember(p.profileImageUrl, p.profileImageBase64, p.updatedAt) {
                                when {
                                    !p.profileImageUrl.isNullOrBlank() -> {
                                        val base = p.profileImageUrl
                                        val sep = if (base.contains('?')) '&' else '?'
                                        "${base}${sep}t=${p.updatedAt.time}"
                                    }
                                    !p.profileImageBase64.isNullOrBlank() -> {
                                        val raw = p.profileImageBase64
                                        val cleaned = raw.substringAfter("base64,", missingDelimiterValue = raw)
                                        try { Base64.decode(cleaned, Base64.DEFAULT) } catch (_: Exception) { null }
                                    }
                                    else -> null
                                }
                            }

                            // Build an ImageRequest, attach Authorization header for URL case
                            val imageRequest = remember(profileImageModel, token) {
                                profileImageModel?.let { model ->
                                    ImageRequest.Builder(context)
                                        .data(model)
                                        .apply {
                                            if (model is String && !token.isNullOrBlank()) {
                                                addHeader("Authorization", "Bearer $token")
                                            }
                                        }
                                        .crossfade(true)
                                        .build()
                                }
                            }

                            val avatarModifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant ?: MaterialTheme.colorScheme.outline, CircleShape)

                            if (imageRequest != null) {
                                SubcomposeAsyncImage(
                                    model = imageRequest,
                                    contentDescription = "Profile image",
                                    contentScale = ContentScale.Crop,
                                    modifier = avatarModifier
                                ) {
                                    when (painter.state) {
                                        is AsyncImagePainter.State.Loading, is AsyncImagePainter.State.Empty -> {
                                            Icon(Icons.Filled.AccountCircle, contentDescription = null)
                                        }
                                        is AsyncImagePainter.State.Error -> {
                                            Icon(Icons.Filled.AccountCircle, contentDescription = null)
                                        }
                                        else -> SubcomposeAsyncImageContent()
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.AccountCircle,
                                    contentDescription = "Profile placeholder",
                                    modifier = avatarModifier
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = p.username, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Text(text = p.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (!p.description.isNullOrBlank()) {
                            Text(text = p.description ?: "", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(onClick = onNavigateToAddHabit) {
                                Text("Add Habit")
                            }
                            OutlinedButton(onClick = onNavigateToEditProfile) {
                                Text("Edit Profile")
                            }
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
