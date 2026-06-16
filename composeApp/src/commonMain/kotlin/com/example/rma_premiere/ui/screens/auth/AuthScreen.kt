package com.example.rma_premiere.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showRegister by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onAuthenticated()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = showRegister,
            transitionSpec = {
                if (targetState) slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                else slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
            }
        ) { isRegister ->
            if (isRegister) {
                RegisterForm(
                    state = state,
                    onRegister = { fullName, username, password ->
                        viewModel.setEvent(AuthContract.UiEvent.Register(fullName, username, password))
                    },
                    onSwitchToLogin = {
                        viewModel.setEvent(AuthContract.UiEvent.ClearError)
                        showRegister = false
                    }
                )
            } else {
                LoginForm(
                    state = state,
                    onLogin = { username, password ->
                        viewModel.setEvent(AuthContract.UiEvent.Login(username, password))
                    },
                    onSwitchToRegister = {
                        viewModel.setEvent(AuthContract.UiEvent.ClearError)
                        showRegister = true
                    }
                )
            }
        }
    }
}

@Composable
private fun LoginForm(
    state: AuthContract.UiState,
    onLogin: (String, String) -> Unit,
    onSwitchToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Showtime", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Sign in to your account", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            }
        )

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = { onLogin(username, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && username.isNotBlank() && password.isNotBlank()
        ) {
            if (state.isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Login")
        }

        TextButton(onClick = onSwitchToRegister) {
            Text("Don't have an account? Register")
        }
    }
}

@Composable
private fun RegisterForm(
    state: AuthContract.UiState,
    onRegister: (String, String, String) -> Unit,
    onSwitchToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val usernameRegex = remember { Regex("^[A-Za-z0-9_]+$") }
    val usernameValid = username.length >= 3 && usernameRegex.matches(username)
    val passwordValid = password.length >= 8

    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Join Showtime today", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = username.isNotEmpty() && !usernameValid,
            supportingText = {
                if (username.isNotEmpty() && !usernameValid) {
                    Text("Min 3 characters: letters, digits and _ only")
                }
            }
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (min 8 characters)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = password.isNotEmpty() && !passwordValid,
            supportingText = {
                if (password.isNotEmpty() && !passwordValid) {
                    Text("Password must have at least 8 characters")
                }
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            }
        )

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = { onRegister(fullName, username, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && fullName.isNotBlank() && usernameValid && passwordValid
        ) {
            if (state.isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Create Account")
        }

        TextButton(onClick = onSwitchToLogin) {
            Text("Already have an account? Login")
        }
    }
}
