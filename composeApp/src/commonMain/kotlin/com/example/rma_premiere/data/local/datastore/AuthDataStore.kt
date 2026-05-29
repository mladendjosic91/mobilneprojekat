package com.example.rma_premiere.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthDataStore(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val FULL_NAME_KEY = stringPreferencesKey("full_name")
    }

    val token: Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }
    val userId: Flow<Int?> = dataStore.data.map { it[USER_ID_KEY] }
    val username: Flow<String?> = dataStore.data.map { it[USERNAME_KEY] }
    val fullName: Flow<String?> = dataStore.data.map { it[FULL_NAME_KEY] }

    suspend fun saveAuth(token: String, userId: Int, username: String, fullName: String) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[USERNAME_KEY] = username
            prefs[FULL_NAME_KEY] = fullName
        }
    }

    suspend fun clearAuth() {
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(USERNAME_KEY)
            prefs.remove(FULL_NAME_KEY)
        }
    }
}
