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

/** Meta-podaci jedne strane kataloga, vraćeni nakon sinhronizacije. */
data class MoviesPageInfo(
    val page: Int,
    val totalPages: Int,
    val totalItems: Int
)

class MoviesRepository(
    private val api: MoviesApi,
    private val moviesDao: MoviesDao
) {
    private companion object {
        const val PAGE_SIZE = 30
    }

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


    /**
     * Dovlači jednu stranu kataloga sa servera i njome ZAMENJUJE sadržaj Room-a
     * (page-by-page: baza drži tačno trenutnu stranu). Vraća meta-podatke za
     * navigaciju strana. Veličina strane (30) ujedno je i bazen za kviz (>= 10).
     */
    suspend fun syncMovies(filters: FilterParams, page: Int = 1): MoviesPageInfo {
        val response = api.getMovies(
            page = page,
            pageSize = PAGE_SIZE,
            sortBy = filters.sortBy,
            sortOrder = filters.sortOrder,
            genreId = filters.genreId,
            query = filters.query.ifBlank { null },
            minYear = filters.minYear,
            maxYear = filters.maxYear,
            minRating = filters.minRating
        )
        val entities = response.items.map { it.toEntity() }
        moviesDao.replaceMovies(entities)
        return MoviesPageInfo(
            page = response.page,
            totalPages = response.totalPages,
            totalItems = response.totalItems
        )
    }

    fun getFilteredMovies(filters: FilterParams): Flow<List<Movie>> {
        val genreSearch = filters.genreId?.let { "\"id\":$it," }
        return moviesDao.getFilteredMovies(
            query = filters.query.ifBlank { null },
            genreSearch = genreSearch,
            minYear = filters.minYear,
            maxYear = filters.maxYear,
            minRating = filters.minRating,
            sortBy = filters.sortBy,
            sortOrder = filters.sortOrder
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
