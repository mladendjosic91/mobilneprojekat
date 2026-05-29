package com.example.rma_premiere

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.rma_premiere.data.repository.AuthRepository
import com.example.rma_premiere.ui.navigation.AppNavigation
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinContext {
        MaterialTheme {
            val authRepository: AuthRepository = koinInject()
            var isLoggedIn by remember { mutableStateOf(false) }
            var isChecking by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                authRepository.init()
                authRepository.token.collect { token ->
                    isLoggedIn = token != null
                    isChecking = false
                }
            }

            if (!isChecking) {
                AppNavigation(isLoggedIn = isLoggedIn)
            }
        }
    }
}
