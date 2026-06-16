package com.example.rma_premiere.ui.screens.watchlist

import com.example.rma_premiere.domain.model.Movie

interface WatchlistContract {

    data class UiState(
        val isLoading: Boolean = false,
        val movies: List<Movie> = emptyList(),
        val error: String? = null,
        val isOffline: Boolean = false
    )

    sealed class UiEvent {
        data object Refresh : UiEvent()
        data class Remove(val movie: Movie) : UiEvent()
    }

    sealed class SideEffect
}
