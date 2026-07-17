package com.example.sportsxtreme.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session_preferences")

class SessionManager(context: Context) {
    private val appContext = context.applicationContext

    val accessTokenFlow: Flow<String?> = appContext.sessionDataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN]
    }

    val isLoggedInFlow: Flow<Boolean> = accessTokenFlow.map { token ->
        !token.isNullOrBlank()
    }

    suspend fun saveAccessToken(token: String) {
        appContext.sessionDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = token
        }
    }

    suspend fun getAccessToken(): String? {
        return accessTokenFlow.first()
    }

    suspend fun clearSession() {
        appContext.sessionDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
    }
}
