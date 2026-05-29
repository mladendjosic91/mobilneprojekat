package com.example.rma_premiere.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect fun createDataStore(): DataStore<Preferences>

internal const val DATASTORE_FILE_NAME = "showtime_auth.preferences_pb"
