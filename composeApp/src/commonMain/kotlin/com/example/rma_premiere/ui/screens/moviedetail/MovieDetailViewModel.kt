package com.example.rma_premiere.ui.screens.moviedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rma_premiere.data.repository.FavoritesRepository
import com.example.rma_premiere.data.repository.MoviesRepository
import com.example.rma_premiere.data.repository.WatchlistRepository
import com.example.rma_premiere.domain.model.Movie
import com.example.rma_premiere.domain.model.MovieDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovieDetailState(
    val isLoading: Boolean = false,
    val movie: MovieDetails? = null,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val isInWatchlist: Boolean = false,
    val toastMessage: String? = null
)

sealed class MovieDetailIntent {
    object Load : MovieDetailIntent()
    object Retry : MovieDetailIntent()
    object ToggleFavorite : MovieDetailIntent()
    object ToggleWatchlist : MovieDetailIntent()
    object ClearToast : MovieDetailIntent()
}

class MovieDetailViewModel(
    private val movieId: String,
    private val moviesRepository: MoviesRepository,
    private val favoritesRepository: FavoritesRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MovieDetailState())
    val state: StateFlow<MovieDetailState> = _state.asStateFlow()

    init {
        onIntent(MovieDetailIntent.Load)
        observeFavoriteWatchlist()
    }

    private fun observeFavoriteWatchlist() {
        viewModelScope.launch {
            combine(
                favoritesRepository.isFavorite(movieId),
                watchlistRepository.isInWatchlist(movieId)
            ) { fav, wl -> fav to wl }.collect { (fav, wl) ->
                _state.update { it.copy(isFavorite = fav, isInWatchlist = wl) }
            }
        }
    }

    fun onIntent(intent: MovieDetailIntent) {
        when (intent) {
            is MovieDetailIntent.Load, MovieDetailIntent.Retry -> loadDetails()
            is MovieDetailIntent.ToggleFavorite -> toggleFavorite()
            is MovieDetailIntent.ToggleWatchlist -> toggleWatchlist()
            is MovieDetailIntent.ClearToast -> _state.update { it.copy(toastMessage = null) }
        }
    }

    private fun loadDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val isFav = favoritesRepository.isFavorite(movieId).let {
                    var result = false
                    it.collect { v -> result = v; return@collect }
                    result
                }
                val isWl = watchlistRepository.isInWatchlist(movieId).let {
                    var result = false
                    it.collect { v -> result = v; return@collect }
                    result
                }
                moviesRepository.syncMovieDetails(movieId, isFav, isWl)
                moviesRepository.getMovieDetails(movieId).collect { details ->
                    _state.update { it.copy(isLoading = false, movie = details) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load movie") }
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
                    _state.update { it.copy(toastMessage = "Removed from favorites") }
                } else {
                    favoritesRepository.addFavorite(movie)
                    moviesRepository.updateFavoriteStatus(movieId, true)
                    _state.update { it.copy(toastMessage = "Added to favorites") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(toastMessage = "Error: ${e.message}") }
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
                    _state.update { it.copy(toastMessage = "Removed from watchlist") }
                } else {
                    watchlistRepository.addToWatchlist(movie)
                    moviesRepository.updateWatchlistStatus(movieId, true)
                    _state.update { it.copy(toastMessage = "Added to watchlist") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(toastMessage = "Error: ${e.message}") }
            }
        }
    }

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
