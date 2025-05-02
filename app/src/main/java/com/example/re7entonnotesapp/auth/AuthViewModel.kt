package com.example.re7entonnotesapp.auth

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.ViewModel
import com.example.re7entonnotesapp.R
import com.example.re7entonnotesapp.presentation.AuthState
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val credentialManager: CredentialManager,
    private val authorizationClient: AuthorizationClient
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    private fun parseEmail(token: String?): String? {
        if (token == null) return null
        val parts = token.split(".")
        if (parts.size < 2) return null
        val json = String(Base64.decode(parts[1], Base64.URL_SAFE))
        return org.json.JSONObject(json)
            .optString("email")
            .takeIf { it.isNotEmpty() }
    }

    /** Silent one-tap sign-in (API-34+). */
    @RequiresApi(34)
    fun trySilentSignIn() {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.server_client_id))
            .setFilterByAuthorizedAccounts(true)
            .build()
        val req = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        credentialManager.getCredentialAsync(
            context, req, null,
            ContextCompat.getMainExecutor(context),
            object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                override fun onResult(result: GetCredentialResponse) {
                    val idToken = (result.credential as GoogleIdTokenCredential).idToken
                    _authState.update { it.copy(idToken = idToken, email = parseEmail(idToken)) }
                }
                override fun onError(e: GetCredentialException) {
                    // silent failed → wait for manual
                }
            }
        )
    }

    /** Interactive one-tap sign-in (API-34+). */
    @RequiresApi(34)
    fun signIn() {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.server_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build()
        val req = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        credentialManager.getCredentialAsync(
            context, req, null,
            ContextCompat.getMainExecutor(context),
            object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                override fun onResult(result: GetCredentialResponse) {
                    val idToken = (result.credential as GoogleIdTokenCredential).idToken
                    _authState.update { it.copy(idToken = idToken, email = parseEmail(idToken)) }
                }
                override fun onError(e: GetCredentialException) {
                    // user dismissed one-tap sheet
                }
            }
        )
    }

    /** Request Drive appDataFolder consent (API-34+). */
    @RequiresApi(34)
    fun requestDriveAuth(onPendingIntent: (PendingIntent) -> Unit) {
        val req = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_APPDATA)))
            .build()
        authorizationClient.authorize(req)
            .addOnSuccessListener { res: AuthorizationResult ->
                if (res.hasResolution()) {
                    onPendingIntent(res.getPendingIntent()!!)
                } else {
                    _authState.update {
                        it.copy(
                            driveAuthorized   = true,
                            driveAccessToken  = res.accessToken
                        )
                    }
                }
            }
    }

    /**
     * Call this from your Activity when the Drive‐consent PendingIntent returns.
     */
    @RequiresApi(34)
    fun handleDriveAuthResponse(data: Intent?) {
        if (data == null) return
        // Turn the Intent back into an AuthorizationResult
        val result: AuthorizationResult = authorizationClient.getAuthorizationResultFromIntent(data)
        if (result.hasResolution()) {
            // user still needs to consent again; you could re‑launch result.getPendingIntent()
        } else {
            // consent granted
            _authState.update { st ->
                st.copy(
                    driveAuthorized   = true,
                    driveAccessToken  = result.accessToken
                )
            }
        }
    }

    /** Sign-out resets everything. */
    fun signOut() {
        _authState.value = AuthState()
    }
}