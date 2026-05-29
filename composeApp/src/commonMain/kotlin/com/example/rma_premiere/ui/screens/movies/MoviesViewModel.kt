package com.example.rma_premiere.ui.screens.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rma_premiere.data.repository.MoviesRepository
import com.example.rma_premiere.domain.model.FilterParams
import com.example.rma_premiere.domain.model.Genre
import com.example.rma_premiere.domain.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MoviesState(
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val error: String? = null,
    val filters: FilterParams = FilterParams(),
    val pendingFilters: FilterParams = FilterParams(),
    val genres: List<Genre> = emptyList(),
    val isSynced: Boolean = false
)

sealed class MoviesIntent {
    object LoadMovies : MoviesIntent()
    object RetryLoad : MoviesIntent()
    data class ApplyFilters(val filters: FilterParams) : MoviesIntent()
    data class UpdatePendingFilters(val filters: FilterParams) : MoviesIntent()
    object ClearFilters : MoviesIntent()
    data class ChangeSortBy(val sortBy: String) : MoviesIntent()
}

class MoviesViewModel(
    private val moviesRepository: MoviesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MoviesState())
    val state: StateFlow<MoviesState> = _state.asStateFlow()

    init {
        onIntent(MoviesIntent.LoadMovies)
    }

    fun onIntent(intent: MoviesIntent) {
        when (intent) {
            is MoviesIntent.LoadMovies -> loadMovies()
            is MoviesIntent.RetryLoad -> loadMovies()
            is MoviesIntent.ApplyFilters -> applyFilters(intent.filters)
            is MoviesIntent.UpdatePendingFilters -> _state.update { it.copy(pendingFilters = intent.filters) }
            is MoviesIntent.ClearFilters -> applyFilters(FilterParams())
            is MoviesIntent.ChangeSortBy -> changeSortBy(intent.sortBy)
        }
    }

    private fun loadMovies() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val genres = moviesRepository.getGenres()
                moviesRepository.syncMovies(_state.value.filters)
                collectMovies()
                _state.update { it.copy(isLoading = false, genres = genres, isSynced = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load movies") }
            }
        }
    }

    private fun applyFilters(filters: FilterParams) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, filters = filters, pendingFilters = filters, error = null) }
            try {
                moviesRepository.syncMovies(filters)
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to filter movies") }
            }
        }
    }

    private fun changeSortBy(sortBy: String) {
        val newFilters = _state.value.filters.copy(sortBy = sortBy)
        applyFilters(newFilters)
    }

    private fun collectMovies() {
        viewModelScope.launch {
            moviesRepository.getFilteredMovies(_state.value.filters).collect { movies ->
                _state.update { it.copy(movies = movies) }
            }
        }
    }
}
