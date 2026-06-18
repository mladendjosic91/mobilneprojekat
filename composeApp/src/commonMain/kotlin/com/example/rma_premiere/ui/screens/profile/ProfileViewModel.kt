package com.example.rma_premiere.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rma_premiere.data.repository.AuthRepository
import com.example.rma_premiere.data.repository.FavoritesRepository
import com.example.rma_premiere.data.repository.QuizRepository
import com.example.rma_premiere.data.repository.WatchlistRepository
import com.example.rma_premiere.domain.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val quizRepository: QuizRepository,
    private val favoritesRepository: FavoritesRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: ProfileContract.UiState.() -> ProfileContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<ProfileContract.UiEvent>()
    fun setEvent(event: ProfileContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        observeEvents()
        observeProfile()
        refreshUser()
        syncQuizHistory()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    ProfileContract.UiEvent.Logout -> logout()
                }
            }
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            combine(
                authRepository.currentUser,
                quizRepository.getBestScore(),
                quizRepository.getTotalPlays(),
                favoritesRepository.getFavoritesCount(),
                watchlistRepository.getWatchlistCount()
            ) { user, bestScore, totalPlays, favCount, wlCount ->
                ProfileData(user, bestScore, totalPlays, favCount, wlCount)
            }.collect { data ->
                setState {
                    copy(
                        user = data.user,
                        bestScore = data.bestScore,
                        totalPlays = data.totalPlays,
                        favoritesCount = data.favoritesCount,
                        watchlistCount = data.watchlistCount
                    )
                }
            }
        }
    }

    private fun refreshUser() {
        viewModelScope.launch {
            val ok = authRepository.refreshCurrentUser()
            setState { copy(isOffline = !ok) }
        }
    }


    private fun syncQuizHistory() {
        viewModelScope.launch {
            runCatching { quizRepository.syncQuizResults() }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            setState { copy(isLoggingOut = true) }
            authRepository.logout()
            setState { copy(isLoggingOut = false) }
        }
    }

    private data class ProfileData(
        val user: User?,
        val bestScore: Float?,
        val totalPlays: Int,
        val favoritesCount: Int,
        val watchlistCount: Int
    )
}
