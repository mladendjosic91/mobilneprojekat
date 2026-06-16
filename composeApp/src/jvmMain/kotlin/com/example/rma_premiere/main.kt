package com.example.rma_premiere

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.rma_premiere.di.appModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

fun main() {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            modules(appModule)
        }
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Showtime",
        ) {
            App()
        }
    }
}
