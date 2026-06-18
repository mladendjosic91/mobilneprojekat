package com.example.rma_premiere.ui.screens.movies

import com.example.rma_premiere.domain.model.FilterParams
import com.example.rma_premiere.domain.model.Genre
import com.example.rma_premiere.domain.model.Movie

interface MoviesContract {

    data class UiState(
        val isLoading: Boolean = false,
        val movies: List<Movie> = emptyList(),
        val error: String? = null,
        val isOffline: Boolean = false,
        val filters: FilterParams = FilterParams(),
        val pendingFilters: FilterParams = FilterParams(),
        val genres: List<Genre> = emptyList(),
        val isSynced: Boolean = false,
        val page: Int = 1,
        val totalPages: Int = 1,
        val totalItems: Int = 0
    ) {
        val canGoPrev: Boolean get() = page > 1 && !isLoading
        val canGoNext: Boolean get() = page < totalPages && !isLoading
    }

    sealed class UiEvent {
        data object LoadMovies : UiEvent()
        data object RetryLoad : UiEvent()
        data object NextPage : UiEvent()
        data object PrevPage : UiEvent()
        data class ApplyFilters(val filters: FilterParams) : UiEvent()
        data class UpdatePendingFilters(val filters: FilterParams) : UiEvent()
        data object ClearFilters : UiEvent()
        data class ChangeSortBy(val sortBy: String) : UiEvent()
        data object ToggleSortOrder : UiEvent()
    }

    sealed class SideEffect
}
