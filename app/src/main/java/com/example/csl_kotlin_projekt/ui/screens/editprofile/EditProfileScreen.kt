package com.example.csl_kotlin_projekt.ui.screens.editprofile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.InputStream
import com.example.csl_kotlin_projekt.util.LogComposableLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditProfileViewModel = viewModel(factory = EditProfileViewModel.factory(LocalContext.current))
) {
    LogComposableLifecycle("EditProfileScreen")
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    // On save success -> exit
    LaunchedEffect(uiState.value.saveSuccess) {
        if (uiState.value.saveSuccess) onSaved()
    }

    var localSelectedImage by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        localSelectedImage = uri
        if (uri != null) {
            // Best effort: detect mime type by contentResolver or fallback
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val name = "profile" + when {
                mimeType.contains("png") -> ".png"
                mimeType.contains("webp") -> ".webp"
                else -> ".jpg"
            }
            val bytes = readAllBytes(context.contentResolver.openInputStream(uri))
            if (bytes != null) {
                viewModel.uploadImage(bytes, name, mimeType)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val disabled = uiState.value.saving || uiState.value.uploading || uiState.value.isLoading
                    IconButton(onClick = { viewModel.saveUsername() }, enabled = !disabled) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
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
            if (uiState.value.isLoading) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }
            uiState.value.error?.let { err ->
                Text(text = err, color = MaterialTheme.colorScheme.error)
            }

            // Profile image + button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val imageModel = localSelectedImage ?: uiState.value.profile?.profileImageUrl
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .clip(CircleShape)
                        .height(80.dp)
                        .fillMaxWidth(0.2f)
                )
                OutlinedButton(onClick = { imagePicker.launch("image/*") }, enabled = !uiState.value.uploading) {
                    if (uiState.value.uploading) {
                        CircularProgressIndicator(modifier = Modifier.height(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.height(4.dp))
                    }
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.height(4.dp))
                    Text("Change photo")
                }
            }

            // Email (read-only)
            Text(text = "Email", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = uiState.value.email, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)

            // Username
            OutlinedTextField(
                value = uiState.value.usernameInput,
                onValueChange = { viewModel.setUsername(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                singleLine = true
            )

            Button(
                onClick = { viewModel.saveUsername() },
                enabled = !uiState.value.saving && !uiState.value.uploading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.value.saving) {
                    CircularProgressIndicator(modifier = Modifier.height(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.height(6.dp))
                }
                Text("Save")
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun readAllBytes(input: InputStream?): ByteArray? {
    if (input == null) return null
    return input.use { it.readBytes() }
}
