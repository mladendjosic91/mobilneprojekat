package com.example.rma_premiere.ui.navigation

import kotlinx.serialization.Serializable

@Serializable object AuthRoute
@Serializable object MoviesRoute
@Serializable data class MovieDetailRoute(val movieId: String)
@Serializable object FavoritesRoute
@Serializable object WatchlistRoute
@Serializable object QuizRoute
@Serializable data class QuizResultRoute(val score: Float, val correctAnswers: Int, val timeUsedSeconds: Int)
@Serializable object ProfileRoute
