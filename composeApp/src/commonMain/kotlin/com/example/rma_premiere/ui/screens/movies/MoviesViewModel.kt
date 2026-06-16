package com.example.rma_premiere.ui.screens.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rma_premiere.data.remote.isNetworkError
import com.example.rma_premiere.data.repository.MoviesRepository
import com.example.rma_premiere.domain.model.FilterParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModel(
    private val moviesRepository: MoviesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MoviesContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: MoviesContract.UiState.() -> MoviesContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<MoviesContract.UiEvent>()
    fun setEvent(event: MoviesContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        observeEvents()
        observeMovies()
        refresh()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    MoviesContract.UiEvent.LoadMovies,
                    MoviesContract.UiEvent.RetryLoad -> refresh()

                    MoviesContract.UiEvent.LoadNextPage -> loadNextPage()

                    is MoviesContract.UiEvent.ApplyFilters -> applyFilters(event.filters)

                    is MoviesContract.UiEvent.UpdatePendingFilters ->
                        setState { copy(pendingFilters = event.filters) }

                    MoviesContract.UiEvent.ClearFilters -> applyFilters(FilterParams())

                    is MoviesContract.UiEvent.ChangeSortBy ->
                        applyFilters(_state.value.filters.copy(sortBy = event.sortBy))

                    MoviesContract.UiEvent.ToggleSortOrder -> {
                        val current = _state.value.filters
                        val flipped = if (current.sortOrder == "desc") "asc" else "desc"
                        applyFilters(current.copy(sortOrder = flipped))
                    }
                }
            }
        }
    }

    // Room je SSOT: jedan kolektor koji se automatski restartuje kad se filteri promene
    private fun observeMovies() {
        viewModelScope.launch {
            _state
                .map { it.filters }
                .distinctUntilChanged()
                .flatMapLatest { filters -> moviesRepository.getFilteredMovies(filters) }
                .collect { movies -> setState { copy(movies = movies) } }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null, isOffline = false, page = 1, endReached = false) }
            try {
                if (_state.value.genres.isEmpty()) {
                    val genres = moviesRepository.getGenres()
                    setState { copy(genres = genres) }
                }
                val hasMore = moviesRepository.syncMovies(_state.value.filters, page = 1)
                setState { copy(isLoading = false, isSynced = true, endReached = !hasMore) }
            } catch (e: Exception) {
                setState {
                    copy(
                        isLoading = false,
                        isOffline = e.isNetworkError,
                        error = if (e.isNetworkError) "No connection. Showing saved movies."
                                else e.message ?: "Failed to load movies"
                    )
                }
            }
        }
    }

    private fun loadNextPage() {
        val current = _state.value
        if (current.isLoading || current.isLoadingMore || current.endReached || !current.isSynced) return
        viewModelScope.launch {
            setState { copy(isLoadingMore = true) }
            try {
                val nextPage = _state.value.page + 1
                val hasMore = moviesRepository.syncMovies(_state.value.filters, page = nextPage)
                setState { copy(isLoadingMore = false, page = nextPage, endReached = !hasMore) }
            } catch (e: Exception) {
                // Greska pri ucitavanju sledece strane ne rusi listu — zadrzavamo postojece podatke
                setState { copy(isLoadingMore = false, isOffline = e.isNetworkError) }
            }
        }
    }

    private fun applyFilters(filters: FilterParams) {
        viewModelScope.launch {
            setState {
                copy(
                    isLoading = true,
                    filters = filters,
                    pendingFilters = filters,
                    error = null,
                    isOffline = false,
                    page = 1,
                    endReached = false
                )
            }
            try {
                val hasMore = moviesRepository.syncMovies(filters, page = 1)
                setState { copy(isLoading = false, isSynced = true, endReached = !hasMore) }
            } catch (e: Exception) {
                setState {
                    copy(
                        isLoading = false,
                        isOffline = e.isNetworkError,
                        error = if (e.isNetworkError) "No connection. Showing saved movies."
                                else e.message ?: "Failed to filter movies"
                    )
                }
            }
        }
    }
}
