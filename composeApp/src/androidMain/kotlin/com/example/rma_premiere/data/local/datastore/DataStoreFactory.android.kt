package com.example.rma_premiere.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.rma_premiere.data.local.db.appContext

actual fun createDataStore(): DataStore<Preferences> {
    return appContext.dataStore
}

private val android.content.Context.dataStore by preferencesDataStore(name = DATASTORE_FILE_NAME)
