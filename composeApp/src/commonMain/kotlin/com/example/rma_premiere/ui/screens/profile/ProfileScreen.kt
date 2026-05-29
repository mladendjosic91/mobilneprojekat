package com.example.rma_premiere.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.user) {
        if (state.user == null && !state.isLoggingOut) {
            // already logged out
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile") }) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // User info
            state.user?.let { user ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(user.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("@${user.username}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Stats
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Statistics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    HorizontalDivider()
                    StatRow("Favorites", state.favoritesCount.toString())
                    StatRow("Watchlist", state.watchlistCount.toString())
                    StatRow("Quizzes played", state.totalPlays.toString())
                    state.bestScore?.let { score ->
                        StatRow("Best quiz score", String.format("%.2f", score))
                    } ?: StatRow("Best quiz score", "—")
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.onIntent(ProfileIntent.Logout)
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                enabled = !state.isLoggingOut
            ) {
                if (state.isLoggingOut) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Logout")
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
