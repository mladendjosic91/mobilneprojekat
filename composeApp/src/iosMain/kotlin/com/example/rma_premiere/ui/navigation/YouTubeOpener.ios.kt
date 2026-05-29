package com.example.rma_premiere.ui.navigation

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openYouTube(key: String) {
    val url = NSURL.URLWithString("https://www.youtube.com/watch?v=$key") ?: return
    UIApplication.sharedApplication.openURL(url)
}
