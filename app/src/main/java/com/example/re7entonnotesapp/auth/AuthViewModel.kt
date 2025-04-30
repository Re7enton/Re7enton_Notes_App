package com.example.re7entonnotesapp.auth

import android.content.Context
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.services.drive.DriveScopes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.re7entonnotesapp.R
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val credentialManager: CredentialManager,
    private val authorizationClient: AuthorizationClient
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    /** Helper to parse email from JWT payload */
    private fun parseEmail(token: String?): String? =
        token
            ?.split(".")
            ?.getOrNull(1)
            ?.let { payloadB64 ->
                val json = String(Base64.decode(payloadB64, Base64.URL_SAFE))
                JSONObject(json)
                    .optString("email")                // returns "" if no “email” field
                    .takeIf { it.isNotEmpty() }        // convert "" → null
            }

    /** 1️⃣ Silent one-tap sign-in */
    fun trySilentSignIn() = viewModelScope.launch {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.server_client_id))
            .setFilterByAuthorizedAccounts(true)
            .build()
        val req = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        try {
            val resp = credentialManager.getCredential(context, req)
            val cred = resp.credential as GoogleIdTokenCredential
            val token = cred.idToken
            _authState.update {
                it.copy(
                    idToken = token,
                    email = parseEmail(token)
                )
            }
        } catch (_: NoCredentialException) {
            // no-op
        } catch (_: GetCredentialException) {
            // no-op
        }
    }

    /** 2️⃣ Interactive sign-in; returns a PendingIntent via callback if needed */
    fun beginInteractiveSignIn(onPendingIntent: (android.app.PendingIntent) -> Unit) = viewModelScope.launch {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.server_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build()
        val req = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        try {
            val resp = credentialManager.getCredential(context, req)
            val token = (resp.credential as GoogleIdTokenCredential).idToken
            _authState.update {
                it.copy(
                    idToken = token,
                    email = parseEmail(token)
                )
            }
        } catch (e: NoCredentialException) {
            // UI must launch this PendingIntent to show the sign-in sheet
            // no saved credential or user dismissed the sheet
            // nothing to do here — UI will remain signed-out until next call
        } catch (_: GetCredentialException) {
            // no-op
        }
    }

    /** 3️⃣ Request Drive-appDataFolder authorization */
    fun requestDriveAuth(onPendingIntent: (android.app.PendingIntent) -> Unit) {
        val authReq = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_APPDATA)))
            .build()
        authorizationClient.authorize(authReq)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    result.getPendingIntent()?.let(onPendingIntent)
                } else {
                    _authState.update { it.copy(
                        driveAuthorized   = true,
                        driveAccessToken  = result.accessToken    // ← capture token
                    ) }
                }
            }
            .addOnFailureListener {
                // handle error if you like
            }
    }

    /** 4️⃣ Sign-out: clear all credentials */
    fun signOut() = viewModelScope.launch {
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        _authState.value = AuthState()   // reset to defaults
    }
}
