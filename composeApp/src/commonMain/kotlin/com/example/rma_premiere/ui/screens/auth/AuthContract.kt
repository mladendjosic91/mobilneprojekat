package com.example.rma_premiere.ui.screens.auth

interface AuthContract {

    data class UiState(
        val isLoading: Boolean = false,
        val isLoggedIn: Boolean = false,
        val error: String? = null,
        val isCheckingAuth: Boolean = true
    )

    sealed class UiEvent {
        data class Login(val username: String, val password: String) : UiEvent()
        data class Register(val fullName: String, val username: String, val password: String) : UiEvent()
        data object Logout : UiEvent()
        data object ClearError : UiEvent()
    }

    sealed class SideEffect
}
