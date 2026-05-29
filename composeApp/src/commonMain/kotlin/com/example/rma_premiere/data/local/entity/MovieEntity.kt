package com.example.rma_premiere.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val imdbId: String,
    val title: String,
    val year: Int?,
    val imdbRating: Float?,
    val imdbVotes: Int?,
    val posterPath: String?,
    val genresJson: String // JSON array of genres
)

@Entity(tableName = "movie_details")
data class MovieDetailsEntity(
    @PrimaryKey val imdbId: String,
    val tmdbId: Int?,
    val title: String,
    val originalTitle: String?,
    val overview: String?,
    val tagline: String?,
    val releaseDate: String?,
    val year: Int?,
    val runtime: Int?,
    val budget: Long?,
    val revenue: Long?,
    val languageCode: String?,
    val popularity: Float?,
    val imdbRating: Float?,
    val imdbVotes: Int?,
    val tmdbRating: Float?,
    val tmdbVotes: Int?,
    val posterPath: String?,
    val backdropPath: String?,
    val homepage: String?,
    val genresJson: String,
    val castJson: String,
    val backdropImagesJson: String,
    val trailersJson: String,
    val isFavorite: Boolean = false,
    val isInWatchlist: Boolean = false
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val imdbId: String,
    val title: String,
    val year: Int?,
    val imdbRating: Float?,
    val imdbVotes: Int?,
    val posterPath: String?,
    val genresJson: String,
    val addedAt: Long = 0L
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val imdbId: String,
    val title: String,
    val year: Int?,
    val imdbRating: Float?,
    val imdbVotes: Int?,
    val posterPath: String?,
    val genresJson: String,
    val addedAt: Long = 0L
)

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: Int,
    val score: Float,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val timeUsedSeconds: Int,
    val playedAt: Long
)
