package com.example.rma_premiere.ui.screens.moviedetail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.rma_premiere.domain.model.CastMember
import com.example.rma_premiere.domain.model.MovieDetails
import com.example.rma_premiere.ui.components.OfflineBanner
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: String,
    onBack: () -> Unit,
    onOpenTrailer: (String) -> Unit,
    viewModel: MovieDetailViewModel = koinViewModel(parameters = { parametersOf(movieId) })
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MovieDetailContract.SideEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.movie?.title ?: "") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(state.error!!)
                    Button(onClick = { viewModel.setEvent(MovieDetailContract.UiEvent.Refresh) }) { Text("Retry") }
                }
            }
            state.movie != null -> Column(modifier = Modifier.padding(padding)) {
                if (state.isOffline) {
                    OfflineBanner()
                }
                MovieDetailContent(
                    movie = state.movie!!,
                    isFavorite = state.isFavorite,
                    isInWatchlist = state.isInWatchlist,
                    onFavoriteToggle = { viewModel.setEvent(MovieDetailContract.UiEvent.ToggleFavorite) },
                    onWatchlistToggle = { viewModel.setEvent(MovieDetailContract.UiEvent.ToggleWatchlist) },
                    onOpenTrailer = onOpenTrailer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MovieDetailContent(
    movie: MovieDetails,
    isFavorite: Boolean,
    isInWatchlist: Boolean,
    onFavoriteToggle: () -> Unit,
    onWatchlistToggle: () -> Unit,
    onOpenTrailer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        // Backdrop with play button
        Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            AsyncImage(
                model = movie.backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }
                    ?: movie.posterPath?.let { "https://image.tmdb.org/t/p/w780$it" },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            movie.trailerKey?.let { key ->
                IconButton(
                    onClick = { onOpenTrailer(key) },
                    modifier = Modifier.align(Alignment.Center).size(64.dp)
                ) {
                    Icon(Icons.Default.PlayCircle, "Play Trailer", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.surface)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = movie.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(100.dp).height(150.dp)
                    .offset(y = (-40).dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onWatchlistToggle) {
                        Icon(
                            if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            "Watchlist",
                            tint = if (isInWatchlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(movie.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Text(
                buildString {
                    movie.year?.let { append(it) }
                    movie.runtime?.let { append(" • ${it} min") }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                movie.imdbRating?.let { rating ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                        Text(String.format("%.1f", rating), fontWeight = FontWeight.Bold)
                        movie.imdbVotes?.let { Text("(${formatVotes(it)})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
                movie.tmdbRating?.let { rating ->
                    Text("TMDB ${String.format("%.1f", rating)}", style = MaterialTheme.typography.bodySmall)
                }
            }


            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                movie.genres.forEach { genre ->
                    SuggestionChip(onClick = {}, label = { Text(genre.name) })
                }
            }

            movie.overview?.let {
                Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }

            Text("Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                movie.budget?.takeIf { it > 0 }?.let { InfoBadge("Budget", formatMoney(it)) }
                movie.revenue?.takeIf { it > 0 }?.let { InfoBadge("Revenue", formatMoney(it)) }
                movie.languageCode?.let { InfoBadge("Language", it.uppercase()) }
                movie.popularity?.let { InfoBadge("Popularity", String.format("%.1f", it)) }
                movie.tmdbVotes?.let { InfoBadge("TMDB Votes", formatVotes(it)) }
            }

            if (movie.backdropImages.isNotEmpty()) {
                Text("Images", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    movie.backdropImages.take(3).forEach { path ->
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w780$path",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.width(240.dp).height(135.dp).clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }

            if (movie.cast.isNotEmpty()) {
                Text("Cast", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    movie.cast.take(10).forEach { member ->
                        CastMemberItem(member)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoBadge(label: String, value: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CastMemberItem(member: CastMember) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = member.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
            contentDescription = member.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(48.dp).clip(CircleShape)
        )
        Column {
            Text(member.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            member.department?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun formatVotes(votes: Int): String = when {
    votes >= 1_000_000 -> "${votes / 1_000_000}M"
    votes >= 1_000 -> "${votes / 1_000}K"
    else -> votes.toString()
}

private fun formatMoney(amount: Long): String = when {
    amount >= 1_000_000_000 -> "\$${amount / 1_000_000_000}B"
    amount >= 1_000_000 -> "\$${amount / 1_000_000}M"
    amount >= 1_000 -> "\$${amount / 1_000}K"
    else -> "\$$amount"
}
