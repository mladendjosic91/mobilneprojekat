package com.example.rma_premiere.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rma_premiere.data.repository.AuthRepository
import com.example.rma_premiere.data.repository.FavoritesRepository
import com.example.rma_premiere.data.repository.QuizRepository
import com.example.rma_premiere.data.repository.WatchlistRepository
import com.example.rma_premiere.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileState(
    val user: User? = null,
    val bestScore: Float? = null,
    val totalPlays: Int = 0,
    val favoritesCount: Int = 0,
    val watchlistCount: Int = 0,
    val isLoggingOut: Boolean = false
)

sealed class ProfileIntent {
    object Logout : ProfileIntent()
}

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val quizRepository: QuizRepository,
    private val favoritesRepository: FavoritesRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                authRepository.currentUser,
                quizRepository.getBestScore(),
                quizRepository.getTotalPlays(),
                favoritesRepository.getFavoritesCount(),
                watchlistRepository.getWatchlistCount()
            ) { user, bestScore, totalPlays, favCount, wlCount ->
                ProfileState(
                    user = user,
                    bestScore = bestScore,
                    totalPlays = totalPlays,
                    favoritesCount = favCount,
                    watchlistCount = wlCount
                )
            }.collect { newState ->
                _state.update { newState }
            }
        }
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.Logout -> logout()
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoggingOut = true) }
            favoritesRepository.clearLocal()
            watchlistRepository.clearLocal()
            quizRepository.clearLocalResults()
            authRepository.logout()
            _state.update { it.copy(isLoggingOut = false) }
        }
    }
}
