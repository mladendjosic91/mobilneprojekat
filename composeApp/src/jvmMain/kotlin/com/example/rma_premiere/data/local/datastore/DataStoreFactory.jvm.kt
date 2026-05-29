package com.example.rma_premiere.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import java.io.File

actual fun createDataStore(): DataStore<Preferences> {
    val path = File(System.getProperty("user.home"), DATASTORE_FILE_NAME).absolutePath
    return createDataStore(path)
}
