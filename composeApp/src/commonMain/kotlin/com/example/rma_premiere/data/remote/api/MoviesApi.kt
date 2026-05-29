package com.example.rma_premiere.data.remote.api

import com.example.rma_premiere.data.remote.dto.CastMemberDto
import com.example.rma_premiere.data.remote.dto.ConfigEntryDto
import com.example.rma_premiere.data.remote.dto.GenreDto
import com.example.rma_premiere.data.remote.dto.MovieDetailsDto
import com.example.rma_premiere.data.remote.dto.MovieImagesDto
import com.example.rma_premiere.data.remote.dto.MovieListItemDto
import com.example.rma_premiere.data.remote.dto.PaginatedResponse
import com.example.rma_premiere.data.remote.dto.VideoDto
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface MoviesApi {

    @GET("movies")
    suspend fun getMovies(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 30,
        @Query("sort_by") sortBy: String = "imdb_rating",
        @Query("sort_order") sortOrder: String = "desc",
        @Query("genre_id") genreId: Int? = null,
        @Query("query") query: String? = null,
        @Query("min_year") minYear: Int? = null,
        @Query("max_year") maxYear: Int? = null,
        @Query("min_rating") minRating: Float? = null
    ): PaginatedResponse<MovieListItemDto>

    @GET("movies/{id}")
    suspend fun getMovieDetails(@Path("id") id: String): MovieDetailsDto

    @GET("movies/{id}/cast")
    suspend fun getMovieCast(
        @Path("id") id: String,
        @Query("page_size") pageSize: Int = 10
    ): PaginatedResponse<CastMemberDto>

    @GET("movies/{id}/images")
    suspend fun getMovieImages(
        @Path("id") id: String,
        @Query("type") type: String? = null
    ): MovieImagesDto

    @GET("movies/{id}/videos")
    suspend fun getMovieVideos(
        @Path("id") id: String,
        @Query("type") type: String? = null
    ): List<VideoDto>

    @GET("genres")
    suspend fun getGenres(): List<GenreDto>

    @GET("config")
    suspend fun getConfig(): List<ConfigEntryDto>
}
