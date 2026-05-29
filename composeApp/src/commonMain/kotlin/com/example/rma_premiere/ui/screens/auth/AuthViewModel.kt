package com.example.rma_premiere.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rma_premiere.data.repository.AuthRepository
import com.example.rma_premiere.data.repository.FavoritesRepository
import com.example.rma_premiere.data.repository.WatchlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val isCheckingAuth: Boolean = true
)

sealed class AuthIntent {
    data class Login(val username: String, val password: String) : AuthIntent()
    data class Register(val fullName: String, val username: String, val password: String) : AuthIntent()
    object Logout : AuthIntent()
    object ClearError : AuthIntent()
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val favoritesRepository: FavoritesRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val token = authRepository.token.first()
            _state.update { it.copy(isLoggedIn = token != null, isCheckingAuth = false) }
        }
    }

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.Login -> login(intent.username, intent.password)
            is AuthIntent.Register -> register(intent.fullName, intent.username, intent.password)
            is AuthIntent.Logout -> logout()
            is AuthIntent.ClearError -> _state.update { it.copy(error = null) }
        }
    }

    private fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                authRepository.login(username, password)
                _state.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = parseError(e)) }
            }
        }
    }

    private fun register(fullName: String, username: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                authRepository.signup(fullName, username, password)
                _state.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = parseError(e)) }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            favoritesRepository.clearLocal()
            watchlistRepository.clearLocal()
            authRepository.logout()
            _state.update { it.copy(isLoading = false, isLoggedIn = false) }
        }
    }

    private fun parseError(e: Exception): String {
        val msg = e.message ?: "Unknown error"
        val type = e::class.simpleName ?: ""
        return when {
            msg.contains("401") || msg.contains("Unauthorized") -> "Invalid username or password"
            msg.contains("409") || msg.contains("Conflict") -> "Username already taken"
            msg.contains("400") -> "Invalid input. Check your fields"
            else -> "$type: $msg"
        }
    }
}
