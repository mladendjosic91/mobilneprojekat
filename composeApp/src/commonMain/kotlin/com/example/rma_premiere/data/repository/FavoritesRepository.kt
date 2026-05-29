package com.example.rma_premiere.data.repository

import com.example.rma_premiere.data.local.dao.FavoritesDao
import com.example.rma_premiere.data.mapper.toDomain
import com.example.rma_premiere.data.mapper.toFavoriteEntity
import com.example.rma_premiere.data.remote.api.ShowtimeApi
import com.example.rma_premiere.domain.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepository(
    private val api: ShowtimeApi,
    private val favoritesDao: FavoritesDao
) {
    fun getFavorites(): Flow<List<Movie>> =
        favoritesDao.getFavorites().map { it.map { e -> e.toDomain() } }

    fun isFavorite(imdbId: String): Flow<Boolean> = favoritesDao.isFavorite(imdbId)

    fun getFavoritesCount(): Flow<Int> = favoritesDao.getFavoritesCount()

    suspend fun syncFavorites() {
        val remote = api.getFavorites()
        val now = System.currentTimeMillis()
        favoritesDao.deleteAllFavorites()
        favoritesDao.insertFavorites(remote.mapIndexed { i, dto ->
            dto.toFavoriteEntity(now - i)
        })
    }

    suspend fun addFavorite(movie: Movie) {
        // Optimistic: insert locally first
        favoritesDao.insertFavorite(movie.toFavoriteEntity())
        try {
            api.addFavorite(movie.imdbId)
        } catch (e: Exception) {
            // Rollback on error
            favoritesDao.removeFavorite(movie.imdbId)
            throw e
        }
    }

    suspend fun removeFavorite(movie: Movie) {
        // Optimistic: remove locally first
        favoritesDao.removeFavorite(movie.imdbId)
        try {
            api.removeFavorite(movie.imdbId)
        } catch (e: Exception) {
            // Rollback on error
            favoritesDao.insertFavorite(movie.toFavoriteEntity())
            throw e
        }
    }

    suspend fun clearLocal() {
        favoritesDao.deleteAllFavorites()
    }
}
