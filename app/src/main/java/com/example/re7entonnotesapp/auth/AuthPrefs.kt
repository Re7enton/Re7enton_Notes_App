package com.example.re7entonnotesapp.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authPrefs by preferencesDataStore(name = "auth_prefs")

class AuthPrefs(private val context: Context) {
    companion object {
        private val KEY_DRIVE_AUTH = booleanPreferencesKey("drive_authorized")
    }

    val driveAuthorizedFlow: Flow<Boolean> =
        context.authPrefs.data.map { prefs -> prefs[KEY_DRIVE_AUTH] ?: false }

    suspend fun setDriveAuthorized(value: Boolean) {
        context.authPrefs.edit { prefs ->
            prefs[KEY_DRIVE_AUTH] = value
        }
    }
}
