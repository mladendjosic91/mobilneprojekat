package com.example.rma_premiere.data.local.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

actual fun createDatabase(): AppDatabase {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "showtime.db")
    return Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .build()
}
