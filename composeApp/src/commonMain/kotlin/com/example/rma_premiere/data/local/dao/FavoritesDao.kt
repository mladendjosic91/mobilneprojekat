package com.example.rma_premiere.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rma_premiere.data.local.entity.FavoriteEntity
import com.example.rma_premiere.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(favorites: List<FavoriteEntity>)

    @Query("DELETE FROM favorites WHERE imdbId = :imdbId")
    suspend fun removeFavorite(imdbId: String)

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE imdbId = :imdbId)")
    fun isFavorite(imdbId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM favorites")
    fun getFavoritesCount(): Flow<Int>

    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavorites()
}

@Dao
interface WatchlistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToWatchlist(item: WatchlistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(items: List<WatchlistEntity>)

    @Query("DELETE FROM watchlist WHERE imdbId = :imdbId")
    suspend fun removeFromWatchlist(imdbId: String)

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getWatchlist(): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE imdbId = :imdbId)")
    fun isInWatchlist(imdbId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM watchlist")
    fun getWatchlistCount(): Flow<Int>

    @Query("DELETE FROM watchlist")
    suspend fun deleteAllWatchlist()
}
