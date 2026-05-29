package com.example.rma_premiere.ui.screens.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rma_premiere.ui.screens.movies.MovieListItem
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onMovieClick: (String) -> Unit,
    viewModel: WatchlistViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Watchlist") }) }
    ) { padding ->
        when {
            state.isLoading && state.movies.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            state.movies.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Your watchlist is empty", style = MaterialTheme.typography.bodyLarge)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.movies.forEach { movie ->
                        Box {
                            MovieListItem(movie = movie, onClick = { onMovieClick(movie.imdbId) })
                            IconButton(
                                onClick = { viewModel.onIntent(WatchlistIntent.Remove(movie)) },
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                            ) {
                                Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
