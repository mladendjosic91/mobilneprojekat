package com.example.rma_premiere.data.remote.api

import com.example.rma_premiere.data.remote.dto.AuthResponseDto
import com.example.rma_premiere.data.remote.dto.LeaderboardEntryDto
import com.example.rma_premiere.data.remote.dto.LoginRequestDto
import com.example.rma_premiere.data.remote.dto.MovieListItemDto
import com.example.rma_premiere.data.remote.dto.PaginatedResponse
import com.example.rma_premiere.data.remote.dto.PostQuizResultRequestDto
import com.example.rma_premiere.data.remote.dto.PostQuizResultResponseDto
import com.example.rma_premiere.data.remote.dto.QuizResultDto
import com.example.rma_premiere.data.remote.dto.SignupRequestDto
import com.example.rma_premiere.data.remote.dto.UserDto
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface ShowtimeApi {

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @GET("me")
    suspend fun getMe(): UserDto

    @GET("me/favorites")
    suspend fun getFavorites(): List<MovieListItemDto>

    @POST("me/favorites/{movieId}")
    suspend fun addFavorite(@Path("movieId") movieId: String): Unit

    @DELETE("me/favorites/{movieId}")
    suspend fun removeFavorite(@Path("movieId") movieId: String)

    @GET("me/watchlist")
    suspend fun getWatchlist(): List<MovieListItemDto>

    @POST("me/watchlist/{movieId}")
    suspend fun addToWatchlist(@Path("movieId") movieId: String): Unit

    @DELETE("me/watchlist/{movieId}")
    suspend fun removeFromWatchlist(@Path("movieId") movieId: String)

    @GET("leaderboard")
    suspend fun getLeaderboard(
        @Query("category") category: Int = 1,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): PaginatedResponse<LeaderboardEntryDto>

    @POST("leaderboard")
    suspend fun submitQuizResult(@Body request: PostQuizResultRequestDto): PostQuizResultResponseDto

    @GET("me/quiz-results")
    suspend fun getMyQuizResults(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): PaginatedResponse<QuizResultDto>
}
