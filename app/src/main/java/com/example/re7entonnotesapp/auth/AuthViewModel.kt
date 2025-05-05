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
import androidx.credentials.CustomCredential
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
    private val authorizationClient: AuthorizationClient,
    /** Shared flow injected from NetworkModule */
    private val authStateFlow: MutableStateFlow<AuthState>
) : ViewModel() {

    /** Expose read‑only AuthState to UI */
    val authState: StateFlow<AuthState> = authStateFlow

    private val _error = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _error

    private fun parseEmail(token: String?): String? =
        token
            ?.split(".")
            ?.getOrNull(1)
            ?.let { String(Base64.decode(it, Base64.URL_SAFE)) }
            ?.let { json -> org.json.JSONObject(json).optString("email").takeIf { it.isNotEmpty() } }

    /** Update the shared flow in one place */
    private fun updateAuth(update: AuthState.() -> AuthState) {
        authStateFlow.update(update)
    }

    @RequiresApi(34)
    fun trySilentSignIn() {
        Log.d(TAG, "trySilentSignIn()")
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.server_client_id))
            .setFilterByAuthorizedAccounts(true)
            .build()
        val req = GetCredentialRequest.Builder().addCredentialOption(option).build()

        credentialManager.getCredentialAsync(
            context, req, null,
            ContextCompat.getMainExecutor(context),
            object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                override fun onResult(result: GetCredentialResponse) {
                    handleIdCredential(result.credential)
                }
                override fun onError(e: GetCredentialException) {
                    Log.d(TAG, "silent sign‑in: no credential", e)
                }
            }
        )
    }

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
                    Log.d(TAG, "signIn: credential=${result.credential::class.java.simpleName}")
                    handleIdCredential(result.credential)
                }
                override fun onError(e: GetCredentialException) {
                    Log.e(TAG, "Interactive sign‑in failed", e)
                    _error.value = context.getString(R.string.sign_in_failed)
                }
            }
        )
    }

    private fun handleIdCredential(credential: androidx.credentials.Credential) {
        when {
            credential is GoogleIdTokenCredential -> {
                val token = credential.idToken
                updateAuth { copy(idToken = token, email = parseEmail(token)) }
            }
            credential is CustomCredential
                    && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                try {
                    // Convert the Bundle inside CustomCredential into a GoogleIdTokenCredential
                    val idCred = GoogleIdTokenCredential.createFrom(credential.data)
                    val token = idCred.idToken
                    updateAuth { copy(idToken = token, email = parseEmail(token)) }
                } catch (ex: Exception) {
                    Log.e(TAG, "Failed to parse ID token", ex)
                    _error.value = context.getString(R.string.invalid_token)
                }
            }
            else -> {
                Log.e(TAG, "Unknown credential type: ${credential::class.java}")
                _error.value = context.getString(R.string.unsupported_credential)
            }
        }
    }

    @RequiresApi(34)
    fun requestDriveAuth(onPendingIntent: (PendingIntent) -> Unit) {
        Log.d(TAG, "requestDriveAuth()")
        val req = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_APPDATA)))
            .build()
        authorizationClient.authorize(req)
            .addOnSuccessListener { res: AuthorizationResult ->
                if (res.hasResolution()) onPendingIntent(res.getPendingIntent()!!)
                else updateAuth { copy(driveAuthorized = true, driveAccessToken = res.accessToken) }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Drive consent error", e)
                _error.value = context.getString(R.string.drive_auth_failed)
            }
    }

    @RequiresApi(34)
    fun handleDriveAuthResponse(data: Intent?) {
        Log.d(TAG, "handleDriveAuthResponse")
        if (data == null) {
            _error.value = context.getString(R.string.no_drive_data)
            return
        }
        val res = authorizationClient.getAuthorizationResultFromIntent(data)
        if (!res.hasResolution()) {
            updateAuth { copy(driveAuthorized = true, driveAccessToken = res.accessToken) }
        } else {
            Log.e(TAG, "Drive consent still needs resolution")
            _error.value = context.getString(R.string.drive_consent_incomplete)
        }
    }

    fun signOut() {
        Log.d(TAG, "signOut()")
        updateAuth { AuthState() }
    }
}