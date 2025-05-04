package com.example.re7entonnotesapp.auth

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetCustomCredentialOption
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.PrepareGetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
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

private const val TAG = "AuthViewModel"

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val credentialManager: CredentialManager,
    private val authorizationClient: AuthorizationClient
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    private val _error = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _error

    private fun parseEmail(token: String?): String? {
        return token
            ?.split(".")
            ?.getOrNull(1)
            ?.let { String(Base64.decode(it, Base64.URL_SAFE)) }
            ?.let { json -> org.json.JSONObject(json).optString("email").takeIf { it.isNotEmpty() } }
    }

    /** Silent one‑tap (no UI) */
    @RequiresApi(34)
    fun trySilentSignIn() {
        Log.d(TAG, "trySilentSignIn()")
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
                    Log.d(TAG, "Silent sign‑in succeeded, token=$idToken")
                    _authState.update { it.copy(idToken = idToken, email = parseEmail(idToken)) }
                }
                override fun onError(e: GetCredentialException) {
                    Log.d(TAG, "Silent sign‑in no credential: $e")
                    // no saved credential → user must tap “Sign In”
                }
            }
        )
    }

    /** Interactive one‑tap “Sign In with Google” */
    @RequiresApi(34)
    fun signIn() {
        Log.d(TAG, "signIn()")
        val option = GetSignInWithGoogleOption.Builder(
            context.getString(R.string.server_client_id)
        ).build()
        val req = GetCredentialRequest.Builder().addCredentialOption(option).build()

        credentialManager.getCredentialAsync(
            context, req, null,
            ContextCompat.getMainExecutor(context),
            object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                override fun onResult(result: GetCredentialResponse) {
                    val idToken = (result.credential as GoogleIdTokenCredential).idToken
                    Log.d(TAG, "Interactive sign‑in succeeded, token=$idToken")
                    _authState.update { it.copy(idToken = idToken, email = parseEmail(idToken)) }
                }
                override fun onError(e: GetCredentialException) {
                    Log.e(TAG, "Interactive sign‑in failed", e)
                    _error.value = "Sign‑in cancelled or failed"
                }
            }
        )
    }

    /** Request Drive appDataFolder consent (shows UI) */
    @RequiresApi(34)
    fun requestDriveAuth(onPendingIntent: (PendingIntent) -> Unit) {
        Log.d(TAG, "requestDriveAuth()")
        val req = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_APPDATA)))
            .build()
        authorizationClient.authorize(req)
            .addOnSuccessListener { res: AuthorizationResult ->
                if (res.hasResolution()) {
                    Log.d(TAG, "Drive consent needs resolution")
                    onPendingIntent(res.getPendingIntent()!!)
                } else {
                    Log.d(TAG, "Drive consent already granted")
                    _authState.update { it.copy(driveAuthorized = true, driveAccessToken = res.accessToken) }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Drive consent error", e)
                _error.value = "Drive authorization failed"
            }
    }

    /** Handle returned Drive‑consent Intent */
    @RequiresApi(34)
    fun handleDriveAuthResponse(data: Intent?) {
        Log.d(TAG, "handleDriveAuthResponse(data=$data)")
        if (data == null) {
            Log.e(TAG, "No data in Drive consent result")
            return
        }
        val res = authorizationClient.getAuthorizationResultFromIntent(data)
        if (!res.hasResolution()) {
            Log.d(TAG, "Drive consent granted, token=${res.accessToken}")
            _authState.update { it.copy(driveAuthorized = true, driveAccessToken = res.accessToken) }
        } else {
            Log.e(TAG, "Drive consent still needs resolution")
        }
    }

    /** Sign‑out clears everything */
    fun signOut() {
        Log.d(TAG, "signOut()")
        _authState.value = AuthState()
    }
}
