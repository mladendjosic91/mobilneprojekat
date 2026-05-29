package com.example.rma_premiere.data.repository

import com.example.rma_premiere.data.local.dao.WatchlistDao
import com.example.rma_premiere.data.mapper.toDomain
import com.example.rma_premiere.data.mapper.toWatchlistEntity
import com.example.rma_premiere.data.remote.api.ShowtimeApi
import com.example.rma_premiere.domain.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WatchlistRepository(
    private val api: ShowtimeApi,
    private val watchlistDao: WatchlistDao
) {
    fun getWatchlist(): Flow<List<Movie>> =
        watchlistDao.getWatchlist().map { it.map { e -> e.toDomain() } }

    fun isInWatchlist(imdbId: String): Flow<Boolean> = watchlistDao.isInWatchlist(imdbId)

    fun getWatchlistCount(): Flow<Int> = watchlistDao.getWatchlistCount()

    suspend fun syncWatchlist() {
        val remote = api.getWatchlist()
        val now = System.currentTimeMillis()
        watchlistDao.deleteAllWatchlist()
        watchlistDao.insertWatchlist(remote.mapIndexed { i, dto ->
            dto.toWatchlistEntity(now - i)
        })
    }

    suspend fun addToWatchlist(movie: Movie) {
        watchlistDao.insertToWatchlist(movie.toWatchlistEntity())
        try {
            api.addToWatchlist(movie.imdbId)
        } catch (e: Exception) {
            watchlistDao.removeFromWatchlist(movie.imdbId)
            throw e
        }
    }

    suspend fun removeFromWatchlist(movie: Movie) {
        watchlistDao.removeFromWatchlist(movie.imdbId)
        try {
            api.removeFromWatchlist(movie.imdbId)
        } catch (e: Exception) {
            watchlistDao.insertToWatchlist(movie.toWatchlistEntity())
            throw e
        }
    }

    suspend fun clearLocal() {
        watchlistDao.deleteAllWatchlist()
    }
}
