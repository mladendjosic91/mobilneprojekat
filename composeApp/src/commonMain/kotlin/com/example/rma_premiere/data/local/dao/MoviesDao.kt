package com.example.rma_premiere.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rma_premiere.data.local.entity.MovieDetailsEntity
import com.example.rma_premiere.data.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoviesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieDetails(details: MovieDetailsEntity)

    @Query("SELECT * FROM movies ORDER BY imdbRating DESC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("""
        SELECT * FROM movies
        WHERE (:genreSearch IS NULL OR genresJson LIKE '%' || :genreSearch || '%')
        AND (:query IS NULL OR title LIKE '%' || :query || '%')
        AND (:minYear IS NULL OR year >= :minYear)
        AND (:maxYear IS NULL OR year <= :maxYear)
        AND (:minRating IS NULL OR imdbRating >= :minRating)
        ORDER BY
            CASE WHEN :sortBy = 'imdb_rating' THEN imdbRating END DESC,
            CASE WHEN :sortBy = 'year' THEN year END DESC,
            CASE WHEN :sortBy = 'imdb_votes' THEN imdbVotes END DESC,
            CASE WHEN :sortBy = 'title' THEN title END ASC,
            imdbRating DESC
    """)
    fun getFilteredMovies(
        query: String? = null,
        genreSearch: String? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        minRating: Float? = null,
        sortBy: String = "imdb_rating"
    ): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movie_details WHERE imdbId = :id")
    fun getMovieDetails(id: String): Flow<MovieDetailsEntity?>

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun getMovieCount(): Int

    @Query("DELETE FROM movies")
    suspend fun deleteAllMovies()

    @Query("UPDATE movie_details SET isFavorite = :isFavorite WHERE imdbId = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("UPDATE movie_details SET isInWatchlist = :isInWatchlist WHERE imdbId = :id")
    suspend fun updateWatchlistStatus(id: String, isInWatchlist: Boolean)
}
