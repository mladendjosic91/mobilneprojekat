package com.example.rma_premiere.data.repository

import com.example.rma_premiere.data.local.datastore.AuthDataStore
import com.example.rma_premiere.data.local.datastore.TokenHolder
import com.example.rma_premiere.data.remote.api.ShowtimeApi
import com.example.rma_premiere.data.remote.dto.LoginRequestDto
import com.example.rma_premiere.data.remote.dto.SignupRequestDto
import com.example.rma_premiere.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class AuthRepository(
    private val api: ShowtimeApi,
    private val authDataStore: AuthDataStore,
    private val favoritesRepository: FavoritesRepository,
    private val watchlistRepository: WatchlistRepository,
    private val quizRepository: QuizRepository
) {
    val token: Flow<String?> = authDataStore.token

    val currentUser: Flow<User?> = combine(
        authDataStore.userId,
        authDataStore.username,
        authDataStore.fullName
    ) { id, username, fullName ->
        if (id != null && username != null && fullName != null)
            User(id, username, fullName)
        else null
    }

    suspend fun init() {
        TokenHolder.token = authDataStore.token.first()
    }

    suspend fun signup(fullName: String, username: String, password: String): User {
        val response = api.signup(SignupRequestDto(fullName, username, password))
        authDataStore.saveAuth(response.accessToken, response.user.id, response.user.username, response.user.fullName)
        TokenHolder.token = response.accessToken
        return User(response.user.id, response.user.username, response.user.fullName)
    }

    suspend fun login(username: String, password: String): User {
        val response = api.login(LoginRequestDto(username, password))
        authDataStore.saveAuth(response.accessToken, response.user.id, response.user.username, response.user.fullName)
        TokenHolder.token = response.accessToken
        return User(response.user.id, response.user.username, response.user.fullName)
    }


    suspend fun refreshCurrentUser(): Boolean {
        return try {
            val token = authDataStore.token.first() ?: return true
            val me = api.getMe()
            authDataStore.saveAuth(token, me.id, me.username, me.fullName)
            true
        } catch (_: Exception) {
            false
        }
    }


    suspend fun logout() {
        TokenHolder.token = null
        favoritesRepository.clearLocal()
        watchlistRepository.clearLocal()
        quizRepository.clearLocalResults()
        authDataStore.clearAuth()
    }
}
