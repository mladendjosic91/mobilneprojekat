package com.example.rma_premiere.ui.screens.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rma_premiere.data.remote.isNetworkError
import com.example.rma_premiere.data.repository.WatchlistRepository
import com.example.rma_premiere.domain.model.Movie
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch

class WatchlistViewModel(
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WatchlistContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: WatchlistContract.UiState.() -> WatchlistContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<WatchlistContract.UiEvent>()
    fun setEvent(event: WatchlistContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        observeEvents()
        observeWatchlist()
        refresh()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    WatchlistContract.UiEvent.Refresh -> refresh()
                    is WatchlistContract.UiEvent.Remove -> remove(event.movie)
                }
            }
        }
    }

    // Room je SSOT — lista uvek dolazi iz lokalne baze
    private fun observeWatchlist() {
        viewModelScope.launch {
            watchlistRepository.getWatchlist().collect { movies ->
                setState { copy(movies = movies) }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null, isOffline = false) }
            try {
                watchlistRepository.syncWatchlist()
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

    private fun remove(movie: Movie) {
        viewModelScope.launch {
            try {
                watchlistRepository.removeFromWatchlist(movie)
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
