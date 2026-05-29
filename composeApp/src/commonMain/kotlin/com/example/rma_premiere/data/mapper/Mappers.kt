package com.example.rma_premiere.data.mapper

import com.example.rma_premiere.data.local.entity.FavoriteEntity
import com.example.rma_premiere.data.local.entity.MovieDetailsEntity
import com.example.rma_premiere.data.local.entity.MovieEntity
import com.example.rma_premiere.data.local.entity.WatchlistEntity
import com.example.rma_premiere.data.remote.dto.CastMemberDto
import com.example.rma_premiere.data.remote.dto.GenreDto
import com.example.rma_premiere.data.remote.dto.MovieDetailsDto
import com.example.rma_premiere.data.remote.dto.MovieListItemDto
import com.example.rma_premiere.domain.model.CastMember
import com.example.rma_premiere.domain.model.Genre
import com.example.rma_premiere.domain.model.Movie
import com.example.rma_premiere.domain.model.MovieDetails
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

// ---- Genre ----
fun GenreDto.toDomain() = Genre(id = id, name = name)

// ---- MovieListItem ----
fun MovieListItemDto.toDomain() = Movie(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
    genres = genres.map { it.toDomain() }
)

fun MovieListItemDto.toEntity(): MovieEntity = MovieEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
    genresJson = json.encodeToString(genres)
)

fun MovieEntity.toDomain(): Movie {
    val genres = try {
        json.decodeFromString<List<GenreDto>>(genresJson).map { it.toDomain() }
    } catch (e: Exception) { emptyList() }
    return Movie(
        imdbId = imdbId,
        title = title,
        year = year,
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        posterPath = posterPath,
        genres = genres
    )
}

// ---- MovieDetails ----
fun MovieDetailsEntity.toDomain(): MovieDetails {
    val genres = try { json.decodeFromString<List<GenreDto>>(genresJson).map { it.toDomain() } } catch (e: Exception) { emptyList() }
    val cast = try { json.decodeFromString<List<CastMemberDto>>(castJson).map { it.toDomain() } } catch (e: Exception) { emptyList() }
    val backdrops = try { json.decodeFromString<List<String>>(backdropImagesJson) } catch (e: Exception) { emptyList() }
    val trailers = try { json.decodeFromString<List<String>>(trailersJson) } catch (e: Exception) { emptyList() }
    return MovieDetails(
        imdbId = imdbId,
        tmdbId = tmdbId,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        tagline = tagline,
        releaseDate = releaseDate,
        year = year,
        runtime = runtime,
        budget = budget,
        revenue = revenue,
        languageCode = languageCode,
        popularity = popularity,
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        tmdbRating = tmdbRating,
        tmdbVotes = tmdbVotes,
        posterPath = posterPath,
        backdropPath = backdropPath,
        homepage = homepage,
        genres = genres,
        cast = cast,
        backdropImages = backdrops,
        trailerKey = trailers.firstOrNull(),
        isFavorite = isFavorite,
        isInWatchlist = isInWatchlist
    )
}

fun MovieDetailsDto.toEntity(
    cast: List<CastMemberDto>,
    backdropImages: List<String>,
    trailerKeys: List<String>,
    isFavorite: Boolean = false,
    isInWatchlist: Boolean = false
): MovieDetailsEntity = MovieDetailsEntity(
    imdbId = imdbId,
    tmdbId = tmdbId,
    title = title,
    originalTitle = originalTitle,
    overview = overview,
    tagline = tagline,
    releaseDate = releaseDate,
    year = year,
    runtime = runtime,
    budget = budget,
    revenue = revenue,
    languageCode = languageCode,
    popularity = popularity,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    tmdbRating = tmdbRating,
    tmdbVotes = tmdbVotes,
    posterPath = posterPath,
    backdropPath = backdropPath,
    homepage = homepage,
    genresJson = json.encodeToString(genres),
    castJson = json.encodeToString(cast),
    backdropImagesJson = json.encodeToString(backdropImages),
    trailersJson = json.encodeToString(trailerKeys),
    isFavorite = isFavorite,
    isInWatchlist = isInWatchlist
)

// ---- CastMember ----
fun CastMemberDto.toDomain() = CastMember(
    imdbId = imdbId,
    name = name,
    profilePath = profilePath,
    department = department
)

// ---- Favorites / Watchlist ----
fun MovieListItemDto.toFavoriteEntity(addedAt: Long = System.currentTimeMillis()) = FavoriteEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
    genresJson = json.encodeToString(genres),
    addedAt = addedAt
)

fun FavoriteEntity.toDomain(): Movie {
    val genres = try { json.decodeFromString<List<GenreDto>>(genresJson).map { it.toDomain() } } catch (e: Exception) { emptyList() }
    return Movie(imdbId = imdbId, title = title, year = year, imdbRating = imdbRating, imdbVotes = imdbVotes, posterPath = posterPath, genres = genres)
}

fun Movie.toFavoriteEntity(addedAt: Long = System.currentTimeMillis()) = FavoriteEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
    genresJson = json.encodeToString(genres.map { GenreDto(it.id, it.name) }),
    addedAt = addedAt
)

fun MovieListItemDto.toWatchlistEntity(addedAt: Long = System.currentTimeMillis()) = WatchlistEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
    genresJson = json.encodeToString(genres),
    addedAt = addedAt
)

fun WatchlistEntity.toDomain(): Movie {
    val genres = try { json.decodeFromString<List<GenreDto>>(genresJson).map { it.toDomain() } } catch (e: Exception) { emptyList() }
    return Movie(imdbId = imdbId, title = title, year = year, imdbRating = imdbRating, imdbVotes = imdbVotes, posterPath = posterPath, genres = genres)
}

fun Movie.toWatchlistEntity(addedAt: Long = System.currentTimeMillis()) = WatchlistEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
    genresJson = json.encodeToString(genres.map { GenreDto(it.id, it.name) }),
    addedAt = addedAt
)
