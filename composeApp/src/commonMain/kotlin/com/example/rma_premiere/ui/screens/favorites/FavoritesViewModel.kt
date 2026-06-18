package com.example.rma_premiere.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rma_premiere.data.remote.isNetworkError
import com.example.rma_premiere.data.repository.FavoritesRepository
import com.example.rma_premiere.domain.model.Movie
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: FavoritesContract.UiState.() -> FavoritesContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<FavoritesContract.UiEvent>()
    fun setEvent(event: FavoritesContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        observeEvents()
        observeFavorites()
        refresh()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    FavoritesContract.UiEvent.Refresh -> refresh()
                    is FavoritesContract.UiEvent.RemoveFavorite -> removeFavorite(event.movie)
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.getFavorites().collect { movies ->
                setState { copy(movies = movies) }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null, isOffline = false) }
            try {
                favoritesRepository.syncFavorites()
                setState { copy(isLoading = false) }
            } catch (e: Exception) {
                setState {
                    copy(
                        isLoading = false,
                        isOffline = e.isNetworkError,
                        error = if (e.isNetworkError) null else e.message
                    )
                }
            }
        }
    }

    private fun removeFavorite(movie: Movie) {
        viewModelScope.launch {
            try {
                favoritesRepository.removeFavorite(movie)
            } catch (e: Exception) {
                setState {
                    copy(
                        isOffline = e.isNetworkError,
                        error = if (e.isNetworkError) "No connection — change reverted" else e.message
                    )
                }
            }
        }
    }
}
