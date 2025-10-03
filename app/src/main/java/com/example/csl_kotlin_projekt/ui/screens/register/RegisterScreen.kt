package com.example.csl_kotlin_projekt.ui.screens.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle successful registration
    LaunchedEffect(uiState.isRegistrationSuccessful) {
        if (uiState.isRegistrationSuccessful) {
            onNavigateToHome()
            viewModel.resetRegistrationSuccess()
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // App Title
                Text(
                    text = "Create Account",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Join Laci's Smexy App",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Username Field
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = viewModel::updateUsername,
                    label = { Text("Username") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "Username")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = uiState.usernameError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Username Error
                if (uiState.usernameError != null) {
                    Text(
                        text = uiState.usernameError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Email Field
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = "Email")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = uiState.emailError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Email Error
                if (uiState.emailError != null) {
                    Text(
                        text = uiState.emailError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Password Field
                var passwordVisible by remember { mutableStateOf(false) }
                
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Password")
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = uiState.passwordError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Password Error
                if (uiState.passwordError != null) {
                    Text(
                        text = uiState.passwordError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Confirm Password Field
                var confirmPasswordVisible by remember { mutableStateOf(false) }
                
                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::updateConfirmPassword,
                    label = { Text("Confirm Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Confirm Password")
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    isError = uiState.confirmPasswordError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Confirm Password Error
                if (uiState.confirmPasswordError != null) {
                    Text(
                        text = uiState.confirmPasswordError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // General Error
                if (uiState.generalError != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.generalError!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Register Button
                Button(
                    onClick = { viewModel.register(context, onNavigateToHome) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (uiState.isLoading) "Creating Account..." else "Create Account")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Login Link
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Already have an account? Sign In",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
