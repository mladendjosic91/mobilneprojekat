package com.example.rma_premiere.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.rma_premiere.data.local.entity.MovieDetailsEntity
import com.example.rma_premiere.data.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoviesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    /** Atomarno zamenjuje sadržaj kataloga trenutnom stranom (page-by-page prikaz). */
    @Transaction
    suspend fun replaceMovies(movies: List<MovieEntity>) {
        deleteAllMovies()
        insertMovies(movies)
    }

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
            CASE WHEN :sortBy = 'imdb_rating' AND :sortOrder = 'desc' THEN imdbRating END DESC,
            CASE WHEN :sortBy = 'imdb_rating' AND :sortOrder = 'asc' THEN imdbRating END ASC,
            CASE WHEN :sortBy = 'year' AND :sortOrder = 'desc' THEN year END DESC,
            CASE WHEN :sortBy = 'year' AND :sortOrder = 'asc' THEN year END ASC,
            CASE WHEN :sortBy = 'imdb_votes' AND :sortOrder = 'desc' THEN imdbVotes END DESC,
            CASE WHEN :sortBy = 'imdb_votes' AND :sortOrder = 'asc' THEN imdbVotes END ASC,
            CASE WHEN :sortBy = 'title' AND :sortOrder = 'desc' THEN title END DESC,
            CASE WHEN :sortBy = 'title' AND :sortOrder = 'asc' THEN title END ASC,
            imdbRating DESC
    """)
    fun getFilteredMovies(
        query: String? = null,
        genreSearch: String? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        minRating: Float? = null,
        sortBy: String = "imdb_rating",
        sortOrder: String = "desc"
    ): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movie_details WHERE imdbId = :id")
    fun getMovieDetails(id: String): Flow<MovieDetailsEntity?>

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun getMovieCount(): Int

    @Query("SELECT COUNT(*) FROM movies WHERE posterPath IS NOT NULL")
    suspend fun getMovieCountWithPoster(): Int

    @Query("DELETE FROM movies")
    suspend fun deleteAllMovies()

    @Query("UPDATE movie_details SET isFavorite = :isFavorite WHERE imdbId = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("UPDATE movie_details SET isInWatchlist = :isInWatchlist WHERE imdbId = :id")
    suspend fun updateWatchlistStatus(id: String, isInWatchlist: Boolean)
}
