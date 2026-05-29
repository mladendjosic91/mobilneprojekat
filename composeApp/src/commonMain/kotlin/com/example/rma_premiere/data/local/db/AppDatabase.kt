package com.example.rma_premiere.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.rma_premiere.data.local.dao.FavoritesDao
import com.example.rma_premiere.data.local.dao.MoviesDao
import com.example.rma_premiere.data.local.dao.QuizDao
import com.example.rma_premiere.data.local.dao.WatchlistDao
import com.example.rma_premiere.data.local.entity.FavoriteEntity
import com.example.rma_premiere.data.local.entity.MovieDetailsEntity
import com.example.rma_premiere.data.local.entity.MovieEntity
import com.example.rma_premiere.data.local.entity.QuizResultEntity
import com.example.rma_premiere.data.local.entity.WatchlistEntity

@Database(
    entities = [
        MovieEntity::class,
        MovieDetailsEntity::class,
        FavoriteEntity::class,
        WatchlistEntity::class,
        QuizResultEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moviesDao(): MoviesDao
    abstract fun favoritesDao(): FavoritesDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun quizDao(): QuizDao
}
