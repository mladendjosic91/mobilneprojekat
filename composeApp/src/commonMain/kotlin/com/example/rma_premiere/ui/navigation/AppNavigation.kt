package com.example.rma_premiere.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.rma_premiere.ui.screens.auth.AuthScreen
import com.example.rma_premiere.ui.screens.favorites.FavoritesScreen
import com.example.rma_premiere.ui.screens.moviedetail.MovieDetailScreen
import com.example.rma_premiere.ui.screens.movies.MoviesScreen
import com.example.rma_premiere.ui.screens.profile.ProfileScreen
import com.example.rma_premiere.ui.screens.quiz.QuizResultScreen
import com.example.rma_premiere.ui.screens.quiz.QuizScreen
import com.example.rma_premiere.ui.screens.watchlist.WatchlistScreen

data class BottomNavItem(val label: String, val icon: ImageVector, val route: Any)

val bottomNavItems = listOf(
    BottomNavItem("Movies", Icons.Default.Home, MoviesRoute),
    BottomNavItem("Favorites", Icons.Default.Favorite, FavoritesRoute),
    BottomNavItem("Watchlist", Icons.Default.Bookmark, WatchlistRoute),
    BottomNavItem("Quiz", Icons.Default.PlayArrow, QuizRoute),
    BottomNavItem("Profile", Icons.Default.Person, ProfileRoute)
)

@Composable
fun AppNavigation(isLoggedIn: Boolean) {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStack?.destination

    val isMainRoute = bottomNavItems.any { item ->
        currentDestination?.hasRoute(item.route::class) == true
    }

    val startDestination: Any = if (isLoggedIn) MoviesRoute else AuthRoute

    Scaffold(
        bottomBar = {
            if (isMainRoute && isLoggedIn) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hasRoute(item.route::class) == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable<AuthRoute> {
                AuthScreen(
                    onAuthenticated = {
                        navController.navigate(MoviesRoute) {
                            popUpTo(AuthRoute) { inclusive = true }
                        }
                    }
                )
            }
            composable<MoviesRoute> {
                MoviesScreen(
                    onMovieClick = { navController.navigate(MovieDetailRoute(it)) }
                )
            }
            composable<MovieDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<MovieDetailRoute>()
                MovieDetailScreen(
                    movieId = route.movieId,
                    onBack = { navController.popBackStack() },
                    onOpenTrailer = { key ->
                        // Handled via platform-specific intent
                        openYouTube(key)
                    }
                )
            }
            composable<FavoritesRoute> {
                FavoritesScreen(
                    onMovieClick = { navController.navigate(MovieDetailRoute(it)) }
                )
            }
            composable<WatchlistRoute> {
                WatchlistScreen(
                    onMovieClick = { navController.navigate(MovieDetailRoute(it)) }
                )
            }
            composable<QuizRoute> {
                QuizScreen(
                    onNavigateToResult = { score, correct, time ->
                        navController.navigate(QuizResultRoute(score, correct, time)) {
                            popUpTo(QuizRoute) { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable<QuizResultRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<QuizResultRoute>()
                QuizResultScreen(
                    score = route.score,
                    correctAnswers = route.correctAnswers,
                    timeUsedSeconds = route.timeUsedSeconds,
                    onPlayAgain = {
                        navController.navigate(QuizRoute) {
                            popUpTo(QuizRoute) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable<ProfileRoute> {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(AuthRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

expect fun openYouTube(key: String)
