package com.example.re7entonnotesapp.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// backing property for DataStore<Preferences>
private val Context.authPrefs by preferencesDataStore(name = "auth_prefs")

/**
 * Preferences DataStore for persisting:
 *  • Drive‑authorized flag
 *  • ID‑token (JWT)
 *  • Drive OAuth access token
 */
class AuthPrefs(private val context: Context) {
    companion object {
        private val KEY_DRIVE_AUTH    = booleanPreferencesKey("drive_authorized")
        private val KEY_ID_TOKEN      = stringPreferencesKey("id_token")
        private val KEY_DRIVE_ACCESS  = stringPreferencesKey("drive_access_token")
    }

    /** Flow of persisted Drive‑authorized flag */
    val driveAuthorizedFlow: Flow<Boolean> =
        context.authPrefs.data.map { it[KEY_DRIVE_AUTH] ?: false }

    /** Flow of persisted ID‑token (or null) */
    val idTokenFlow: Flow<String?> =
        context.authPrefs.data.map { it[KEY_ID_TOKEN] }

    /** Flow of persisted Drive OAuth access token (or null) */
    val driveAccessTokenFlow: Flow<String?> =
        context.authPrefs.data.map { it[KEY_DRIVE_ACCESS] }

    /** Save the Drive‑authorized flag */
    suspend fun setDriveAuthorized(value: Boolean) {
        context.authPrefs.edit { it[KEY_DRIVE_AUTH] = value }
    }

    /** Save the ID‑token (or remove if null) */
    suspend fun setIdToken(token: String?) {
        context.authPrefs.edit {
            if (token != null) it[KEY_ID_TOKEN] = token else it.remove(KEY_ID_TOKEN)
        }
    }

    /** Save the Drive access‑token */
    suspend fun setDriveAccessToken(token: String) {
        context.authPrefs.edit { it[KEY_DRIVE_ACCESS] = token }
    }

    /** Clear all stored keys (used on sign‑out) */
    suspend fun clearAll() {
        context.authPrefs.edit {
            it.remove(KEY_DRIVE_AUTH)
            it.remove(KEY_ID_TOKEN)
            it.remove(KEY_DRIVE_ACCESS)
        }
    }
}