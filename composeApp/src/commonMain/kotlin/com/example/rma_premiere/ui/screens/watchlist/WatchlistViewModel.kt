package com.example.rma_premiere.ui.screens.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rma_premiere.data.repository.WatchlistRepository
import com.example.rma_premiere.domain.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WatchlistState(
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val error: String? = null
)

sealed class WatchlistIntent {
    object Load : WatchlistIntent()
    data class Remove(val movie: Movie) : WatchlistIntent()
}

class WatchlistViewModel(
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WatchlistState())
    val state: StateFlow<WatchlistState> = _state.asStateFlow()

    init {
        observeWatchlist()
        sync()
    }

    private fun observeWatchlist() {
        viewModelScope.launch {
            watchlistRepository.getWatchlist().collect { movies ->
                _state.update { it.copy(movies = movies) }
            }
        }
    }

    private fun sync() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                watchlistRepository.syncWatchlist()
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onIntent(intent: WatchlistIntent) {
        when (intent) {
            is WatchlistIntent.Load -> sync()
            is WatchlistIntent.Remove -> remove(intent.movie)
        }
    }

    private fun remove(movie: Movie) {
        viewModelScope.launch {
            try {
                watchlistRepository.removeFromWatchlist(movie)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}
