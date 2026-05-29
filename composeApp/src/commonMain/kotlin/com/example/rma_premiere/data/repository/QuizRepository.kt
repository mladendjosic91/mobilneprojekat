package com.example.rma_premiere.data.repository

import com.example.rma_premiere.data.local.dao.MoviesDao
import com.example.rma_premiere.data.local.dao.QuizDao
import com.example.rma_premiere.data.local.entity.QuizResultEntity
import com.example.rma_premiere.data.mapper.toDomain
import com.example.rma_premiere.data.remote.api.MoviesApi
import com.example.rma_premiere.data.remote.api.ShowtimeApi
import com.example.rma_premiere.data.remote.dto.PostQuizResultRequestDto
import com.example.rma_premiere.domain.model.Movie
import com.example.rma_premiere.domain.model.QuizQuestion
import com.example.rma_premiere.domain.model.QuizQuestionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class QuizRepository(
    private val moviesApi: MoviesApi,
    private val showtimeApi: ShowtimeApi,
    private val moviesDao: MoviesDao,
    private val quizDao: QuizDao,
    private val moviesRepository: MoviesRepository
) {
    fun getBestScore(): Flow<Float?> = quizDao.getBestScore(1)
    fun getTotalPlays(): Flow<Int> = quizDao.getTotalPlays(1)
    fun getAllResults(): Flow<List<QuizResultEntity>> = quizDao.getAllResults()

    suspend fun canStartQuiz(): Boolean {
        return moviesDao.getMovieCount() >= 10
    }

    suspend fun generateQuiz(): List<QuizQuestion> {
        val allMovies = moviesDao.getAllMovies().first().map { it.toDomain() }
        if (allMovies.size < 10) return emptyList()

        val questions = mutableListOf<QuizQuestion>()
        val usedMovieIds = mutableSetOf<String>()
        val typeCounts = mutableMapOf(
            QuizQuestionType.GUESS_MOVIE to 0,
            QuizQuestionType.GUESS_YEAR to 0,
            QuizQuestionType.GUESS_ACTOR to 0
        )

        val shuffled = allMovies.shuffled()

        for (movie in shuffled) {
            if (questions.size >= 10) break

            // Determine which type to use
            val availableTypes = QuizQuestionType.values().filter { typeCounts[it]!! < 4 }
            if (availableTypes.isEmpty()) break

            val type = availableTypes.random()
            val question = generateQuestion(type, movie, allMovies, usedMovieIds) ?: continue

            questions.add(question)
            usedMovieIds.add(movie.imdbId)
            typeCounts[type] = typeCounts[type]!! + 1
        }

        return questions.shuffled()
    }

    private suspend fun generateQuestion(
        type: QuizQuestionType,
        movie: Movie,
        allMovies: List<Movie>,
        usedMovieIds: Set<String>
    ): QuizQuestion? {
        return when (type) {
            QuizQuestionType.GUESS_MOVIE -> generateGuessMovieQuestion(movie, allMovies, usedMovieIds)
            QuizQuestionType.GUESS_YEAR -> generateGuessYearQuestion(movie, allMovies)
            QuizQuestionType.GUESS_ACTOR -> generateGuessActorQuestion(movie, allMovies)
        }
    }

    private fun generateGuessMovieQuestion(
        movie: Movie,
        allMovies: List<Movie>,
        usedMovieIds: Set<String>
    ): QuizQuestion? {
        val imageUrl = moviesRepository.buildPosterUrl(movie.posterPath) ?: return null
        if (movie.imdbId in usedMovieIds) return null

        val wrongMovies = allMovies
            .filter { it.imdbId != movie.imdbId && it.title != movie.title }
            .shuffled()
            .take(3)
        if (wrongMovies.size < 3) return null

        val options = (listOf(movie.title) + wrongMovies.map { it.title }).shuffled()
        return QuizQuestion(
            type = QuizQuestionType.GUESS_MOVIE,
            movieImdbId = movie.imdbId,
            imageUrl = imageUrl,
            movieTitle = null,
            options = options,
            correctAnswer = movie.title
        )
    }

    private fun generateGuessYearQuestion(movie: Movie, allMovies: List<Movie>): QuizQuestion? {
        val year = movie.year ?: return null
        val posterUrl = moviesRepository.buildPosterUrl(movie.posterPath) ?: return null

        val offsets = listOf(-1, -2, -3, -4, -5, -7, -10, 1, 2, 3, 4, 5, 7, 10)
            .shuffled()
            .map { year + it }
            .filter { it != year && it > 1900 && it <= 2025 }
            .distinct()
            .take(3)
        if (offsets.size < 3) return null

        val options = (listOf(year.toString()) + offsets.map { it.toString() }).shuffled()
        return QuizQuestion(
            type = QuizQuestionType.GUESS_YEAR,
            movieImdbId = movie.imdbId,
            imageUrl = posterUrl,
            movieTitle = movie.title,
            options = options,
            correctAnswer = year.toString()
        )
    }

    private suspend fun generateGuessActorQuestion(movie: Movie, allMovies: List<Movie>): QuizQuestion? {
        return try {
            val cast = moviesApi.getMovieCast(movie.imdbId, 10).items
            if (cast.size < 3) return null
            val correctActor = cast.take(3).random()
            val posterUrl = moviesRepository.buildPosterUrl(movie.posterPath) ?: return null

            // Get wrong actors from other movies
            val wrongActors = allMovies
                .filter { it.imdbId != movie.imdbId }
                .shuffled()
                .take(5)
                .flatMap { m ->
                    try {
                        moviesApi.getMovieCast(m.imdbId, 5).items
                    } catch (e: Exception) { emptyList() }
                }
                .filter { it.name != correctActor.name }
                .distinctBy { it.name }
                .shuffled()
                .take(3)

            if (wrongActors.size < 3) return null
            val options = (listOf(correctActor.name) + wrongActors.map { it.name }).shuffled()

            QuizQuestion(
                type = QuizQuestionType.GUESS_ACTOR,
                movieImdbId = movie.imdbId,
                imageUrl = posterUrl,
                movieTitle = movie.title,
                options = options,
                correctAnswer = correctActor.name
            )
        } catch (e: Exception) { null }
    }

    suspend fun saveResult(score: Float, correctAnswers: Int, totalQuestions: Int, timeUsedSeconds: Int) {
        quizDao.insertQuizResult(
            QuizResultEntity(
                category = 1,
                score = score,
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                timeUsedSeconds = timeUsedSeconds,
                playedAt = System.currentTimeMillis()
            )
        )
        try {
            showtimeApi.submitQuizResult(PostQuizResultRequestDto(score = score, category = 1))
        } catch (_: Exception) {}
    }

    suspend fun clearLocalResults() {
        quizDao.deleteAllResults()
    }
}
