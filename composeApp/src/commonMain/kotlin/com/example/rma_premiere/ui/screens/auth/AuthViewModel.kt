package com.example.rma_premiere.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rma_premiere.data.remote.ApiException
import com.example.rma_premiere.data.remote.isNetworkError
import com.example.rma_premiere.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: AuthContract.UiState.() -> AuthContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<AuthContract.UiEvent>()
    fun setEvent(event: AuthContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        observeEvents()
        checkAuthStatus()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is AuthContract.UiEvent.Login -> login(event.username, event.password)
                    is AuthContract.UiEvent.Register -> register(event.fullName, event.username, event.password)
                    AuthContract.UiEvent.Logout -> logout()
                    AuthContract.UiEvent.ClearError -> setState { copy(error = null) }
                }
            }
        }
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val token = authRepository.token.first()
            setState { copy(isLoggedIn = token != null, isCheckingAuth = false) }
        }
    }

    private fun login(username: String, password: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            try {
                authRepository.login(username, password)
                setState { copy(isLoading = false, isLoggedIn = true) }
            } catch (e: Exception) {
                setState { copy(isLoading = false, error = parseError(e)) }
            }
        }
    }

    private fun register(fullName: String, username: String, password: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            try {
                authRepository.signup(fullName, username, password)
                setState { copy(isLoading = false, isLoggedIn = true) }
            } catch (e: Exception) {
                setState { copy(isLoading = false, error = parseError(e)) }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            authRepository.logout()
            setState { copy(isLoading = false, isLoggedIn = false) }
        }
    }

    private fun parseError(e: Exception): String = when {
        e.isNetworkError -> "Network error. Check your connection and try again."
        e is ApiException && e.statusCode == 401 -> "Invalid username or password"
        e is ApiException && e.statusCode == 409 -> "Username already taken"
        e is ApiException && e.statusCode == 400 -> e.serverMessage ?: "Invalid input. Check your fields"
        e is ApiException -> e.serverMessage ?: "Server error (${e.statusCode})"
        else -> e.message ?: "Unknown error"
    }
}
