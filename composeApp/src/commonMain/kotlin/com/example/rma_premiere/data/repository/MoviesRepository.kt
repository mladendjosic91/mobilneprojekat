package com.example.rma_premiere.data.repository

import com.example.rma_premiere.data.local.dao.MoviesDao
import com.example.rma_premiere.data.mapper.toEntity
import com.example.rma_premiere.data.mapper.toDomain
import com.example.rma_premiere.data.remote.api.MoviesApi
import com.example.rma_premiere.domain.model.FilterParams
import com.example.rma_premiere.domain.model.Genre
import com.example.rma_premiere.domain.model.Movie
import com.example.rma_premiere.domain.model.MovieDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MoviesRepository(
    private val api: MoviesApi,
    private val moviesDao: MoviesDao
) {
    private var imageBaseUrl: String = "https://image.tmdb.org/t/p/"
    private var posterSize: String = "w342"
    private var backdropSize: String = "w780"
    private var profileSize: String = "w185"

    suspend fun syncConfig() {
        try {
            val config = api.getConfig()
            imageBaseUrl = config.find { it.key == "image_base_url" }?.value ?: imageBaseUrl
            posterSize = "w342"
            backdropSize = "w780"
            profileSize = "w185"
        } catch (_: Exception) {}
    }

    fun buildImageUrl(path: String?, size: String = posterSize): String? {
        if (path == null) return null
        return "$imageBaseUrl$size$path"
    }

    fun buildPosterUrl(path: String?) = buildImageUrl(path, posterSize)
    fun buildBackdropUrl(path: String?) = buildImageUrl(path, backdropSize)
    fun buildProfileUrl(path: String?) = buildImageUrl(path, profileSize)

    suspend fun syncMovies(filters: FilterParams) {
        try {
            val response = api.getMovies(
                pageSize = 100,
                sortBy = filters.sortBy,
                genreId = filters.genreId,
                query = filters.query.ifBlank { null },
                minYear = filters.minYear,
                maxYear = filters.maxYear,
                minRating = filters.minRating
            )
            val entities = response.items.map { it.toEntity() }
            if (filters.query.isBlank() && filters.genreId == null &&
                filters.minYear == null && filters.maxYear == null && filters.minRating == null) {
                moviesDao.deleteAllMovies()
            }
            moviesDao.insertMovies(entities)
        } catch (e: Exception) {
            throw e
        }
    }

    fun getFilteredMovies(filters: FilterParams): Flow<List<Movie>> {
        val genreSearch = filters.genreId?.toString()
        return moviesDao.getFilteredMovies(
            query = filters.query.ifBlank { null },
            genreSearch = genreSearch,
            minYear = filters.minYear,
            maxYear = filters.maxYear,
            minRating = filters.minRating,
            sortBy = filters.sortBy
        ).map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun syncMovieDetails(id: String, isFavorite: Boolean = false, isInWatchlist: Boolean = false) {
        val dto = api.getMovieDetails(id)
        val castDto = api.getMovieCast(id, 10).items
        val imagesDto = api.getMovieImages(id, "backdrop")
        val videosDto = api.getMovieVideos(id, "Trailer")
        val backdropPaths = imagesDto.backdrops.take(3).map { it.filePath }
        val trailerKeys = videosDto.take(1).map { it.key }
        val entity = dto.toEntity(castDto, backdropPaths, trailerKeys, isFavorite, isInWatchlist)
        moviesDao.insertMovieDetails(entity)
    }

    fun getMovieDetails(id: String): Flow<MovieDetails?> =
        moviesDao.getMovieDetails(id).map { it?.toDomain() }

    suspend fun getGenres(): List<Genre> =
        api.getGenres().map { it.toDomain() }

    suspend fun getMovieCount(): Int = moviesDao.getMovieCount()

    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean) {
        moviesDao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun updateWatchlistStatus(id: String, isInWatchlist: Boolean) {
        moviesDao.updateWatchlistStatus(id, isInWatchlist)
    }
}
