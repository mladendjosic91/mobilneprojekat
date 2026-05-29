package com.example.rma_premiere.ui.navigation

import android.content.Intent
import android.net.Uri
import com.example.rma_premiere.data.local.db.appContext

actual fun openYouTube(key: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$key"))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    appContext.startActivity(intent)
}
