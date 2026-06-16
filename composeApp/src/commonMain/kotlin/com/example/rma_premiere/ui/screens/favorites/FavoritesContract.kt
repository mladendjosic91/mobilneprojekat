package com.example.rma_premiere.ui.screens.favorites

import com.example.rma_premiere.domain.model.Movie

interface FavoritesContract {

    data class UiState(
        val isLoading: Boolean = false,
        val movies: List<Movie> = emptyList(),
        val error: String? = null,
        val isOffline: Boolean = false
    )

    sealed class UiEvent {
        data object Refresh : UiEvent()
        data class RemoveFavorite(val movie: Movie) : UiEvent()
    }

    sealed class SideEffect
}
