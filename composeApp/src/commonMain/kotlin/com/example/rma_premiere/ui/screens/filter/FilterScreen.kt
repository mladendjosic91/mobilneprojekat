package com.example.rma_premiere.ui.screens.filter

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rma_premiere.domain.model.FilterParams
import com.example.rma_premiere.domain.model.Genre
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    currentFilters: FilterParams,
    genres: List<Genre>,
    onApply: (FilterParams) -> Unit,
    onBack: () -> Unit,
    onClear: () -> Unit,
    onFilterChange: (FilterParams) -> Unit
) {
    val currentYear = 2025
    val minYearRange = 1900f
    val maxYearRange = currentYear.toFloat()

    var searchQuery by remember { mutableStateOf(currentFilters.query) }
    var selectedGenreId by remember { mutableStateOf(currentFilters.genreId) }
    var yearRange by remember {
        mutableStateOf(
            (currentFilters.minYear?.toFloat() ?: minYearRange) to
                    (currentFilters.maxYear?.toFloat() ?: maxYearRange)
        )
    }
    var minRating by remember { mutableStateOf(currentFilters.minRating ?: 0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filter Movies") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        searchQuery = ""
                        selectedGenreId = null
                        yearRange = minYearRange to maxYearRange
                        minRating = 0f
                        onClear()
                    }) { Text("Clear All") }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        onApply(
                            FilterParams(
                                query = searchQuery,
                                genreId = selectedGenreId,
                                genreName = genres.find { it.id == selectedGenreId }?.name,
                                minYear = yearRange.first.roundToInt().takeIf { it > minYearRange.roundToInt() },
                                maxYear = yearRange.second.roundToInt().takeIf { it < maxYearRange.roundToInt() },
                                minRating = minRating.takeIf { it > 0f },
                                sortBy = currentFilters.sortBy
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) { Text("Apply Filters") }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Genre
            if (genres.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Genre", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedGenreId == null,
                            onClick = { selectedGenreId = null },
                            label = { Text("All") }
                        )
                        genres.forEach { genre ->
                            FilterChip(
                                selected = selectedGenreId == genre.id,
                                onClick = {
                                    selectedGenreId = if (selectedGenreId == genre.id) null else genre.id
                                },
                                label = { Text(genre.name) }
                            )
                        }
                    }
                }
            }

            // Year range
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Year Range: ${yearRange.first.roundToInt()} – ${yearRange.second.roundToInt()}", style = MaterialTheme.typography.titleSmall)
                RangeSlider(
                    value = yearRange.first..yearRange.second,
                    onValueChange = { yearRange = it.start to it.endInclusive },
                    valueRange = minYearRange..maxYearRange,
                    steps = 0
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Minimum Rating: ${String.format("%.1f", minRating)}", style = MaterialTheme.typography.titleSmall)
                Slider(
                    value = minRating,
                    onValueChange = { minRating = it },
                    valueRange = 0f..10f,
                    steps = 99
                )
            }
        }
    }
}
