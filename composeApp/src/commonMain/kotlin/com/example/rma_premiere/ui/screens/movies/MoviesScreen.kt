package com.example.rma_premiere.ui.screens.movies

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.rma_premiere.domain.model.FilterParams
import com.example.rma_premiere.domain.model.Movie
import com.example.rma_premiere.ui.components.OfflineBanner
import com.example.rma_premiere.ui.screens.filter.FilterScreen
import org.koin.compose.viewmodel.koinViewModel

private val SORT_OPTIONS = listOf(
    "imdb_rating" to "Rating",
    "year" to "Year",
    "title" to "Title",
    "imdb_votes" to "Popularity"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    onMovieClick: (String) -> Unit,
    viewModel: MoviesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showFilter by remember { mutableStateOf(false) }
    var showSortDropdown by remember { mutableStateOf(false) }

    if (showFilter) {
        FilterScreen(
            currentFilters = state.pendingFilters,
            genres = state.genres,
            onApply = { filters ->
                viewModel.setEvent(MoviesContract.UiEvent.ApplyFilters(filters))
                showFilter = false
            },
            onBack = {
                viewModel.setEvent(MoviesContract.UiEvent.UpdatePendingFilters(state.filters))
                showFilter = false
            },
            onClear = {
                viewModel.setEvent(MoviesContract.UiEvent.UpdatePendingFilters(FilterParams()))
            },
            onFilterChange = { viewModel.setEvent(MoviesContract.UiEvent.UpdatePendingFilters(it)) }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movies") },
                actions = {
                    BadgedBox(
                        badge = {
                            if (state.filters.activeFilterCount > 0)
                                Badge { Text(state.filters.activeFilterCount.toString()) }
                        }
                    ) {
                        IconButton(onClick = { showFilter = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isOffline) {
                OfflineBanner()
            }
            // Sort pill + movie count row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${state.movies.size} movies",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.setEvent(MoviesContract.UiEvent.ToggleSortOrder) },
                        label = {
                            Icon(
                                if (state.filters.sortOrder == "desc") Icons.Default.ArrowDownward
                                else Icons.Default.ArrowUpward,
                                contentDescription = "Sort order",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    Box {
                        FilterChip(
                            selected = true,
                            onClick = { showSortDropdown = true },
                            label = { Text("Sort: ${SORT_OPTIONS.find { it.first == state.filters.sortBy }?.second ?: "Rating"}") }
                        )
                        DropdownMenu(
                            expanded = showSortDropdown,
                            onDismissRequest = { showSortDropdown = false }
                        ) {
                            SORT_OPTIONS.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.setEvent(MoviesContract.UiEvent.ChangeSortBy(key))
                                        showSortDropdown = false
                                    },
                                    leadingIcon = {
                                        if (state.filters.sortBy == key)
                                            RadioButton(selected = true, onClick = null)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            when {
                state.isLoading && state.movies.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null && state.movies.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(state.error!!, style = MaterialTheme.typography.bodyLarge)
                            Button(onClick = { viewModel.setEvent(MoviesContract.UiEvent.RetryLoad) }) { Text("Retry") }
                        }
                    }
                }
                state.movies.isEmpty() && state.isSynced -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No movies found", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                else -> {
                    val listState = rememberLazyListState()

                    // Paginacija: kada se priblizimo dnu liste, trazimo sledecu stranicu
                    val shouldLoadMore by remember {
                        derivedStateOf {
                            val info = listState.layoutInfo
                            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
                            info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - 4
                        }
                    }
                    LaunchedEffect(shouldLoadMore) {
                        if (shouldLoadMore) viewModel.setEvent(MoviesContract.UiEvent.LoadNextPage)
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.movies, key = { it.imdbId }) { movie ->
                            MovieListItem(movie = movie, onClick = { onMovieClick(movie.imdbId) })
                        }
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(Modifier.size(28.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListItem(movie: Movie, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            // Poster
            AsyncImage(
                model = movie.posterPath?.let { "https://image.tmdb.org/t/p/w185$it" },
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(93.dp).fillMaxHeight()
            )
            // Info
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        movie.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    movie.year?.let {
                        Text(
                            it.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // IMDB Rating
                    movie.imdbRating?.let { rating ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                            Text(
                                buildString {
                                    append(String.format("%.1f", rating))
                                    movie.imdbVotes?.let { append(" (${formatVotes(it)})") }
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                // Genre chips
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    movie.genres.take(3).forEach { genre ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(genre.name, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
        }
    }
}

fun formatVotes(votes: Int): String {
    return when {
        votes >= 1_000_000 -> "${votes / 1_000_000}M"
        votes >= 1_000 -> "${votes / 1_000}K"
        else -> votes.toString()
    }
}
