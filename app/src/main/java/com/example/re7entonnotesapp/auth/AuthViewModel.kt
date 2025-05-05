package com.example.re7entonnotesapp.auth

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
import androidx.lifecycle.viewModelScope
import com.example.re7entonnotesapp.R
import com.example.re7entonnotesapp.presentation.AuthState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AuthViewModel"

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val credentialManager: CredentialManager,
    private val authorizationClient: AuthorizationClient,
    private val authPrefs: AuthPrefs,
    private val authStateFlow: MutableStateFlow<AuthState>
) : ViewModel() {

    /** Expose read‑only AuthState to UI */
    val authState: StateFlow<AuthState> = authStateFlow

    private val _error = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _error

    init {
        // 1) restore persisted ID‑token → update AuthState
        viewModelScope.launch {
            authPrefs.idTokenFlow.collect { token ->
                token?.let {
                    val email = parseEmail(it)
                    Log.d(TAG, "restored idToken; email=$email")
                    authStateFlow.update { st -> st.copy(idToken = it, email = email) }
                }
            }
        }
        // 2) restore persisted Drive‑authorized flag
        viewModelScope.launch {
            authPrefs.driveAuthorizedFlow.collect { granted ->
                if (granted) {
                    Log.d(TAG, "restored driveAuthorized=true")
                    authStateFlow.update { st -> st.copy(driveAuthorized = true) }
                }
            }
        }
        // 3) restore persisted Drive access‑token
        viewModelScope.launch {
            authPrefs.driveAccessTokenFlow.collect { token ->
                token?.let {
                    Log.d(TAG, "restored driveAccessToken")
                    authStateFlow.update { st -> st.copy(driveAccessToken = it) }
                }
            }
        }
        // 4) once we've loaded any saved ID‑token, attempt silent sign‑in only if none
        viewModelScope.launch {
            val existing = authPrefs.idTokenFlow.first()
            if (existing == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                trySilentSignIn()
            }
        }
    }

    /** Parse “email” claim from JWT payload */
    private fun parseEmail(token: String?): String? =
        token
            ?.split(".")
            ?.getOrNull(1)
            ?.let { String(Base64.decode(it, Base64.URL_SAFE)) }
            ?.let { org.json.JSONObject(it).optString("email").takeIf { e -> e.isNotEmpty() } }

    /** Common helper to update AuthState */
    private fun updateAuth(transform: AuthState.() -> AuthState) {
        authStateFlow.update(transform)
    }

    /** Silent one‑tap sign‑in (no UI) on API‑34+. */
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
                override fun onResult(r: GetCredentialResponse) = handleCredential(r.credential)
                override fun onError(e: GetCredentialException) {
                    Log.d(TAG, "silent sign‑in failed", e)
                }
            }
        )
    }

    /** Interactive “Sign In with Google” (shows UI). */
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
                override fun onResult(r: GetCredentialResponse) {
                    Log.d(TAG, "signIn: credential=${r.credential::class.simpleName}")
                    handleCredential(r.credential)
                }
                override fun onError(e: GetCredentialException) {
                    Log.e(TAG, "interactive sign‑in failed", e)
                    _error.value = context.getString(R.string.sign_in_failed)
                }
            }
        )
    }

    /** Handle whatever Credential comes back (silent or interactive). */
    private fun handleCredential(cred: androidx.credentials.Credential) {
        when {
            cred is GoogleIdTokenCredential -> {
                val t = cred.idToken
                viewModelScope.launch { authPrefs.setIdToken(t) }
                updateAuth { copy(idToken = t, email = parseEmail(t)) }
            }
            cred is CustomCredential
                    && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                try {
                    val idCred = GoogleIdTokenCredential.createFrom(cred.data)
                    val t = idCred.idToken
                    viewModelScope.launch { authPrefs.setIdToken(t) }
                    updateAuth { copy(idToken = t, email = parseEmail(t)) }
                } catch (ex: Exception) {
                    Log.e(TAG, "invalid ID token", ex)
                    _error.value = context.getString(R.string.invalid_token)
                }
            }
            else -> {
                Log.e(TAG, "unknown credential: ${cred::class}")
                _error.value = context.getString(R.string.unsupported_credential)
            }
        }
    }

    /** Request Drive appDataFolder consent (shows UI). */
    @RequiresApi(34)
    fun requestDriveAuth(onPendingIntent: (PendingIntent) -> Unit) {
        Log.d(TAG, "requestDriveAuth()")
        val req = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_APPDATA)))
            .build()
        authorizationClient.authorize(req)
            .addOnSuccessListener { res: AuthorizationResult ->
                if (res.hasResolution()) {
                    onPendingIntent(res.getPendingIntent()!!)
                } else {
                    // consent granted immediately → capture & persist
                    val at = res.accessToken!!
                    updateAuth { copy(driveAuthorized = true, driveAccessToken = at) }
                    viewModelScope.launch {
                        authPrefs.setDriveAuthorized(true)
                        authPrefs.setDriveAccessToken(at)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Drive consent error", e)
                _error.value = context.getString(R.string.drive_auth_failed)
            }
    }

    /** Handle Drive consent result Intent. */
    @RequiresApi(34)
    fun handleDriveAuthResponse(data: Intent?) {
        Log.d(TAG, "handleDriveAuthResponse")
        if (data == null) {
            _error.value = context.getString(R.string.no_drive_data)
            return
        }
        val res = authorizationClient.getAuthorizationResultFromIntent(data)
        if (!res.hasResolution()) {
            val at = res.accessToken!!
            updateAuth { copy(driveAuthorized = true, driveAccessToken = at) }
            viewModelScope.launch {
                authPrefs.setDriveAuthorized(true)
                authPrefs.setDriveAccessToken(at)
            }
        } else {
            Log.e(TAG, "Drive consent still needs resolution")
            _error.value = context.getString(R.string.drive_consent_incomplete)
        }
    }

    /** Sign‑out resets everything and clears persisted prefs. */
    fun signOut() {
        Log.d(TAG, "signOut()")
        updateAuth { AuthState() }
        viewModelScope.launch { authPrefs.clearAll() }
    }
}