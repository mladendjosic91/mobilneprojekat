package com.example.rma_premiere.ui.navigation

import java.awt.Desktop
import java.net.URI

actual fun openYouTube(key: String) {
    try {
        Desktop.getDesktop().browse(URI("https://www.youtube.com/watch?v=$key"))
    } catch (_: Exception) {}
}
