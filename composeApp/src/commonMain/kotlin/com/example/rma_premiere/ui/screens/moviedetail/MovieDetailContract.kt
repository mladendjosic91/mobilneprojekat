package com.example.rma_premiere.ui.screens.moviedetail

import com.example.rma_premiere.domain.model.MovieDetails

interface MovieDetailContract {

    data class UiState(
        val isLoading: Boolean = false,
        val movie: MovieDetails? = null,
        val error: String? = null,
        val isOffline: Boolean = false,
        val isFavorite: Boolean = false,
        val isInWatchlist: Boolean = false
    )

    sealed class UiEvent {
        data object Refresh : UiEvent()
        data object ToggleFavorite : UiEvent()
        data object ToggleWatchlist : UiEvent()
    }

    sealed class SideEffect {
        data class ShowMessage(val message: String) : SideEffect()
    }
}
