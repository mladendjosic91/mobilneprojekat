package com.example.rma_premiere.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import java.io.File

actual fun createDataStore(): DataStore<Preferences> {
    val file = File(System.getProperty("user.home"), DATASTORE_FILE_NAME)
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { file.absolutePath.toPath() }
    )
}
