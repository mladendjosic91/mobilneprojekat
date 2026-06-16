package com.example.rma_premiere.ui.screens.moviedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rma_premiere.data.remote.isNetworkError
import com.example.rma_premiere.data.repository.FavoritesRepository
import com.example.rma_premiere.data.repository.MoviesRepository
import com.example.rma_premiere.data.repository.WatchlistRepository
import com.example.rma_premiere.domain.model.Movie
import com.example.rma_premiere.domain.model.MovieDetails
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch

class MovieDetailViewModel(
    private val movieId: String,
    private val moviesRepository: MoviesRepository,
    private val favoritesRepository: FavoritesRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MovieDetailContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: MovieDetailContract.UiState.() -> MovieDetailContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<MovieDetailContract.UiEvent>()
    fun setEvent(event: MovieDetailContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _effects = MutableSharedFlow<MovieDetailContract.SideEffect>()
    val effects = _effects.asSharedFlow()

    private fun setEffect(effect: MovieDetailContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        observeEvents()
        observeMovieDetails()
        observeFavoriteWatchlist()
        refresh()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    MovieDetailContract.UiEvent.Refresh -> refresh()
                    MovieDetailContract.UiEvent.ToggleFavorite -> toggleFavorite()
                    MovieDetailContract.UiEvent.ToggleWatchlist -> toggleWatchlist()
                }
            }
        }
    }

    // Room je SSOT: jedan trajni kolektor, refresh samo upisuje u bazu
    private fun observeMovieDetails() {
        viewModelScope.launch {
            moviesRepository.getMovieDetails(movieId).collect { details ->
                if (details != null) {
                    setState { copy(movie = details, isLoading = false, error = null) }
                }
            }
        }
    }

    private fun observeFavoriteWatchlist() {
        viewModelScope.launch {
            combine(
                favoritesRepository.isFavorite(movieId),
                watchlistRepository.isInWatchlist(movieId)
            ) { fav, wl -> fav to wl }.collect { (fav, wl) ->
                setState { copy(isFavorite = fav, isInWatchlist = wl) }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            setState { copy(isLoading = movie == null, error = null, isOffline = false) }
            try {
                val isFav = favoritesRepository.isFavorite(movieId).first()
                val isWl = watchlistRepository.isInWatchlist(movieId).first()
                moviesRepository.syncMovieDetails(movieId, isFav, isWl)
            } catch (e: Exception) {
                setState {
                    if (movie != null) {
                        // Kesirani detalji postoje — prikazujemo ih u offline rezimu
                        copy(isLoading = false, isOffline = e.isNetworkError)
                    } else {
                        copy(
                            isLoading = false,
                            isOffline = e.isNetworkError,
                            error = if (e.isNetworkError) "No connection. Movie details not cached yet."
                                    else e.message ?: "Failed to load movie"
                        )
                    }
                }
            }
        }
    }

    private fun toggleFavorite() {
        val movie = _state.value.movie?.toMovie() ?: return
        viewModelScope.launch {
            try {
                if (_state.value.isFavorite) {
                    favoritesRepository.removeFavorite(movie)
                    moviesRepository.updateFavoriteStatus(movieId, false)
                    setEffect(MovieDetailContract.SideEffect.ShowMessage("Removed from favorites"))
                } else {
                    favoritesRepository.addFavorite(movie)
                    moviesRepository.updateFavoriteStatus(movieId, true)
                    setEffect(MovieDetailContract.SideEffect.ShowMessage("Added to favorites"))
                }
            } catch (e: Exception) {
                // Optimisticka izmena je vec vracena u repository-ju — javljamo gresku
                setEffect(MovieDetailContract.SideEffect.ShowMessage(toggleErrorMessage(e)))
            }
        }
    }

    private fun toggleWatchlist() {
        val movie = _state.value.movie?.toMovie() ?: return
        viewModelScope.launch {
            try {
                if (_state.value.isInWatchlist) {
                    watchlistRepository.removeFromWatchlist(movie)
                    moviesRepository.updateWatchlistStatus(movieId, false)
                    setEffect(MovieDetailContract.SideEffect.ShowMessage("Removed from watchlist"))
                } else {
                    watchlistRepository.addToWatchlist(movie)
                    moviesRepository.updateWatchlistStatus(movieId, true)
                    setEffect(MovieDetailContract.SideEffect.ShowMessage("Added to watchlist"))
                }
            } catch (e: Exception) {
                setEffect(MovieDetailContract.SideEffect.ShowMessage(toggleErrorMessage(e)))
            }
        }
    }

    private fun toggleErrorMessage(e: Exception): String =
        if (e.isNetworkError) "No connection — change reverted"
        else "Error: ${e.message}"

    private fun MovieDetails.toMovie() = Movie(
        imdbId = imdbId,
        title = title,
        year = year,
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        posterPath = posterPath,
        genres = genres
    )
}
