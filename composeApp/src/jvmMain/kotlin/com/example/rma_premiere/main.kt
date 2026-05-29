package com.example.rma_premiere

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Rmapremiere",
    ) {
        App()
    }
}