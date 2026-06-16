package com.example.rma_premiere.di

import com.example.rma_premiere.data.local.datastore.AuthDataStore
import com.example.rma_premiere.data.local.datastore.TokenHolder
import com.example.rma_premiere.data.local.datastore.createDataStore
import com.example.rma_premiere.data.local.db.AppDatabase
import com.example.rma_premiere.data.local.db.createDatabase
import com.example.rma_premiere.data.remote.ApiException
import com.example.rma_premiere.data.remote.api.MoviesApi
import com.example.rma_premiere.data.remote.api.ShowtimeApi
import com.example.rma_premiere.data.remote.dto.ApiErrorDto
import com.example.rma_premiere.data.repository.AuthRepository
import com.example.rma_premiere.data.repository.FavoritesRepository
import com.example.rma_premiere.data.repository.MoviesRepository
import com.example.rma_premiere.data.repository.QuizRepository
import com.example.rma_premiere.data.repository.WatchlistRepository
import com.example.rma_premiere.ui.screens.auth.AuthViewModel
import com.example.rma_premiere.ui.screens.favorites.FavoritesViewModel
import com.example.rma_premiere.ui.screens.moviedetail.MovieDetailViewModel
import com.example.rma_premiere.ui.screens.movies.MoviesViewModel
import com.example.rma_premiere.ui.screens.profile.ProfileViewModel
import com.example.rma_premiere.ui.screens.quiz.QuizViewModel
import com.example.rma_premiere.ui.screens.watchlist.WatchlistViewModel
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val jsonConfig = Json { ignoreUnknownKeys = true; isLenient = true }

val appModule = module {

    // ---- DataStore & Auth ----
    single { createDataStore() }
    single { AuthDataStore(get()) }

    // ---- HTTP Clients ----
    single<HttpClient>(named("public")) {
        HttpClient {
            install(ContentNegotiation) { json(jsonConfig) }
            install(Logging) { level = LogLevel.INFO }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }

    single<HttpClient>(named("auth")) {
        val koinScope = this
        HttpClient {
            install(ContentNegotiation) { json(jsonConfig) }
            install(Logging) { level = LogLevel.INFO }
            defaultRequest {
                contentType(ContentType.Application.Json)
                val token = TokenHolder.token
                if (token != null) {
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }
            }
            HttpResponseValidator {
                validateResponse { response ->
                    if (!response.status.isSuccess()) {
                        val wasAuthenticated =
                            response.call.request.headers.contains(HttpHeaders.Authorization)
                        // Svaki 401 na autentikovani zahtev pokrece prinudnu odjavu
                        if (response.status == HttpStatusCode.Unauthorized && wasAuthenticated) {
                            koinScope.get<AuthRepository>().logout()
                        }
                        val serverMessage = runCatching {
                            jsonConfig.decodeFromString<ApiErrorDto>(response.bodyAsText()).message
                        }.getOrNull()
                        throw ApiException(response.status.value, serverMessage)
                    }
                }
            }
        }
    }

    // ---- Ktorfit ----
    single<MoviesApi> {
        val client = get<HttpClient>(named("public"))
        Ktorfit.Builder()
            .baseUrl("https://rma.finlab.rs/")
            .httpClient(client)
            .build()
            .create<MoviesApi>()
    }

    single<ShowtimeApi> {
        val client = get<HttpClient>(named("auth"))
        Ktorfit.Builder()
            .baseUrl("https://rma.finlab.rs/")
            .httpClient(client)
            .build()
            .create<ShowtimeApi>()
    }

    // ---- Database ----
    single<AppDatabase> { createDatabase() }
    single { get<AppDatabase>().moviesDao() }
    single { get<AppDatabase>().favoritesDao() }
    single { get<AppDatabase>().watchlistDao() }
    single { get<AppDatabase>().quizDao() }

    // ---- Repositories ----
    single { MoviesRepository(get(), get()) }
    single { FavoritesRepository(get(), get()) }
    single { WatchlistRepository(get(), get()) }
    single { QuizRepository(get(), get(), get(), get(), get()) }
    single { AuthRepository(get(), get(), get(), get(), get()) }

    // ---- ViewModels ----
    viewModel { AuthViewModel(get()) }
    viewModel { MoviesViewModel(get()) }
    viewModel { (movieId: String) -> MovieDetailViewModel(movieId, get(), get(), get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { WatchlistViewModel(get()) }
    viewModel { QuizViewModel(get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
}
