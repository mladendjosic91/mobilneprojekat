package com.example.rma_premiere.ui.screens.profile

import com.example.rma_premiere.domain.model.User

interface ProfileContract {

    data class UiState(
        val user: User? = null,
        val bestScore: Float? = null,
        val totalPlays: Int = 0,
        val favoritesCount: Int = 0,
        val watchlistCount: Int = 0,
        val isLoggingOut: Boolean = false,
        val isOffline: Boolean = false
    )

    sealed class UiEvent {
        data object Logout : UiEvent()
    }

    sealed class SideEffect
}
