package com.example.rma_premiere.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rma_premiere.data.repository.FavoritesRepository
import com.example.rma_premiere.domain.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesState(
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val error: String? = null
)

sealed class FavoritesIntent {
    object Load : FavoritesIntent()
    data class RemoveFavorite(val movie: Movie) : FavoritesIntent()
}

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state.asStateFlow()

    init {
        observeFavorites()
        syncFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.getFavorites().collect { movies ->
                _state.update { it.copy(movies = movies) }
            }
        }
    }

    private fun syncFavorites() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                favoritesRepository.syncFavorites()
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.Load -> syncFavorites()
            is FavoritesIntent.RemoveFavorite -> removeFavorite(intent.movie)
        }
    }

    private fun removeFavorite(movie: Movie) {
        viewModelScope.launch {
            try {
                favoritesRepository.removeFavorite(movie)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}
