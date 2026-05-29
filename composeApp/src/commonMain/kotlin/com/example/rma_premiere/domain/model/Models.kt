package com.example.rma_premiere.domain.model

data class Genre(val id: Int, val name: String)

data class Movie(
    val imdbId: String,
    val title: String,
    val year: Int?,
    val imdbRating: Float?,
    val imdbVotes: Int?,
    val posterPath: String?,
    val genres: List<Genre>
)

data class MovieDetails(
    val imdbId: String,
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
    val genres: List<Genre>,
    val cast: List<CastMember>,
    val backdropImages: List<String>,
    val trailerKey: String?,
    val isFavorite: Boolean,
    val isInWatchlist: Boolean
)

data class CastMember(
    val imdbId: String,
    val name: String,
    val profilePath: String?,
    val department: String?
)

data class User(
    val id: Int,
    val username: String,
    val fullName: String
)

data class QuizQuestion(
    val type: QuizQuestionType,
    val movieImdbId: String,
    val imageUrl: String?,
    val movieTitle: String?,
    val options: List<String>,
    val correctAnswer: String
)

enum class QuizQuestionType {
    GUESS_MOVIE,
    GUESS_YEAR,
    GUESS_ACTOR
}

data class QuizSessionResult(
    val score: Float,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val timeUsedSeconds: Int
)

data class FilterParams(
    val query: String = "",
    val genreId: Int? = null,
    val genreName: String? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val minRating: Float? = null,
    val sortBy: String = "imdb_rating"
) {
    val activeFilterCount: Int get() {
        var count = 0
        if (query.isNotBlank()) count++
        if (genreId != null) count++
        if (minYear != null || maxYear != null) count++
        if (minRating != null) count++
        return count
    }
}
