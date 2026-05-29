package com.example.rma_premiere.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

lateinit var appContext: Context

actual fun createDatabase(): AppDatabase {
    val dbFile = appContext.getDatabasePath("showtime.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
        .setDriver(BundledSQLiteDriver())
        .build()
}
