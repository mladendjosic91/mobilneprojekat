package com.example.rma_premiere.data.local.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSHomeDirectory

actual fun createDatabase(): AppDatabase {
    val dbFile = NSHomeDirectory() + "/showtime.db"
    return Room.databaseBuilder<AppDatabase>(name = dbFile)
        .setDriver(BundledSQLiteDriver())
        .build()
}
